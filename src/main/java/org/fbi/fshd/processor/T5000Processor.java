package org.fbi.fshd.processor;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.fshd.domain.cbs.T5000Request.CbsTia5000;
import org.fbi.fshd.domain.cbs.T5000Response.CbsToa5000;
import org.fbi.fshd.domain.cbs.T5000Response.CbsToa5000Item;
import org.fbi.fshd.domain.tps.T5000Request.TpsTia5000;
import org.fbi.fshd.domain.tps.T5000Request.TpsTia5000Item;
import org.fbi.fshd.domain.tps.T5000Response.TpsToa5000;
import org.fbi.fshd.enums.BillStatus;
import org.fbi.fshd.enums.TxnRtnCode;
import org.fbi.fshd.helper.FbiBeanUtils;
import org.fbi.fshd.helper.MybatisFactory;
import org.fbi.fshd.helper.ProjectConfigManager;
import org.fbi.fshd.repository.dao.FsHdPaymentInfoMapper;
import org.fbi.fshd.repository.dao.FsHdPaymentItemMapper;
import org.fbi.fshd.repository.model.FsHdPaymentInfo;
import org.fbi.fshd.repository.model.FsHdPaymentInfoExample;
import org.fbi.fshd.repository.model.FsHdPaymentItem;
import org.fbi.fshd.repository.model.FsHdPaymentItemExample;
import org.fbi.linking.codec.dataformat.FixedLengthTextDataFormat;
import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhanrui on 14-1-24.
 * ���˽���
 */
public class T5000Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        CbsTia5000 cbsTia;
        try {
            cbsTia = unmarshalCbsRequestMsg(request.getRequestBody());
        } catch (Exception e) {
            logger.error("��ɫҵ��ƽ̨�����Ľ�������.", e);
            marshalAbnormalCbsResponse(TxnRtnCode.CBSMSG_UNMARSHAL_FAILED, null, response);
            return;
        }

        //��鱾�����ݿ���Ϣ
        List<FsHdPaymentInfo> paymentInfos = selectNotCanceledPaymentInfosFromDB(cbsTia.getStratDate(), cbsTia.getEndDate());
        List<CbsToa5000Item> cbsToaItems = new ArrayList<>();
        boolean result = true;

        int totalCnt = 0;
        BigDecimal totalAmt = new BigDecimal("0.00");
        int succCnt = 0;
        BigDecimal succAmt = new BigDecimal("0.00");
        int failCnt = 0;
        BigDecimal failAmt = new BigDecimal("0.00");

        for (FsHdPaymentInfo paymentInfo : paymentInfos) {
            totalCnt++;
            totalAmt = totalAmt.add(paymentInfo.getPayAmt());
            List<FsHdPaymentItem> paymentItems = selectPaymentItemsFromDB(paymentInfo);
            //������ͨѶ����
            TpsTia5000 tpsTia = new TpsTia5000();
            TpsToa5000 tpsToa;

            try {
                FbiBeanUtils.copyProperties(paymentInfo, tpsTia);
                tpsTia.setFisCode(ProjectConfigManager.getInstance().getProperty("tps.fis.fiscode"));
                tpsTia.setTxnHdlCode("A");   //������
                tpsTia.setFisActno(ProjectConfigManager.getInstance().getProperty("tps.fis.actno"));
                tpsTia.setFisBatchSn("000001");   //���κ�����Ϣ

                List<TpsTia5000Item> tpsTiaItems = new ArrayList<>();
                for (FsHdPaymentItem paymentItem : paymentItems) {
                    TpsTia5000Item tpsTiaItem = new TpsTia5000Item();
                    FbiBeanUtils.copyProperties(paymentItem, tpsTiaItem);
                    tpsTiaItems.add(tpsTiaItem);
                }
                tpsTia.setItems(tpsTiaItems);
                tpsTia.setItemNum(paymentItems.size() + "");

                byte[] recvTpsBuf = processThirdPartyServer(marshalTpsRequestMsg(tpsTia), "5000");
                tpsToa = unmarshalTpsResponseMsg(recvTpsBuf);
            } catch (SocketTimeoutException e) {
                logger.error("�������������ͨѶ����ʱ.", e);
                marshalAbnormalCbsResponse(TxnRtnCode.MSG_RECV_TIMEOUT, "�������������ͨѶ����ʱ", response);
                return;
            } catch (Exception e) {
                logger.error("�������������ͨѶ�����쳣.", e);
                marshalAbnormalCbsResponse(TxnRtnCode.MSG_COMM_ERROR, "�������������ͨѶ�����쳣", response);
                return;
            }

            //�����������Ӧ����
            if ("0".equals(tpsToa.getRtnCode())) { //���˳ɹ�
                try {
                    processTxn(paymentInfo, request);
                } catch (Exception e) {
                    marshalAbnormalCbsResponse(TxnRtnCode.TXN_EXECUTE_FAILED, e.getMessage(), response);
                    logger.error("ҵ����ʧ��.", e);
                }
                succCnt++;
                succAmt = succAmt.add(paymentInfo.getPayAmt());
            } else {  //����TPS���ش�����
                String errmsg = getTpsRtnErrorMsg(tpsToa.getRtnCode());
                CbsToa5000Item cbsToaItem = new CbsToa5000Item();
                FbiBeanUtils.copyProperties(paymentInfo, cbsToaItem);
                cbsToaItem.setRtnMsg("[" + tpsToa.getRtnCode() + "]" + errmsg);
                cbsToaItems.add(cbsToaItem);

                result = false;
                failCnt++;
                failAmt = failAmt.add(paymentInfo.getPayAmt());
            }
        }

        CbsToa5000 cbsToa = new CbsToa5000();
        if (result) {
            cbsToa.setRtnCode("0000");
            cbsToa.setRtnMsg("���˳ɹ����ܱ���:[" + totalCnt + "] �ܽ��:[" + totalAmt.toString() + "]");
        } else {
            cbsToa.setRtnCode("1000");
            cbsToa.setRtnMsg("����ʧ�ܣ��ɹ�����:[" + succCnt + "] �ɹ����:[" + succAmt.toString() + "]��ʧ�ܱ���:[" + failCnt + "] ʧ�ܽ��:[" + failAmt.toString() + "]");
            cbsToa.setItemNum(cbsToaItems.size()+"");
            cbsToa.setItems(cbsToaItems);
        }

        //��ɫƽ̨��Ӧ
        String cbsRespMsg = marshalCbsResponseMsg(cbsToa);
        response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_SECCESS.getCode());
        response.setResponseBody(cbsRespMsg.getBytes(response.getCharacterEncoding()));
    }

    //�������CBS������BEAN
    private CbsTia5000 unmarshalCbsRequestMsg(byte[] body) throws Exception {
        CbsTia5000 tia = new CbsTia5000();
        SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
        tia = (CbsTia5000) dataFormat.fromMessage(new String(body, "GBK"), "CbsTia5000");
        return tia;
    }

    //���ݱ������ݿ��е��ѱ�����Ϣ����CBS��Ӧ����
    private String generateCbsRespMsgByLocalDbInfo(FsHdPaymentInfo paymentInfo, List<FsHdPaymentItem> paymentItems) {
        CbsToa5000 cbsToa = new CbsToa5000();
        FbiBeanUtils.copyProperties(paymentInfo, cbsToa);

        List<CbsToa5000Item> cbsToaItems = new ArrayList<>();
        for (FsHdPaymentItem paymentItem : paymentItems) {
            CbsToa5000Item cbsToaItem = new CbsToa5000Item();
            FbiBeanUtils.copyProperties(paymentItem, cbsToaItem);
            cbsToaItems.add(cbsToaItem);
        }
        cbsToa.setItems(cbsToaItems);
        cbsToa.setItemNum("" + cbsToaItems.size());

        String cbsRespMsg = "";
        Map<String, Object> modelObjectsMap = new HashMap<String, Object>();
        modelObjectsMap.put(cbsToa.getClass().getName(), cbsToa);
        SeperatedTextDataFormat cbsDataFormat = new SeperatedTextDataFormat(cbsToa.getClass().getPackage().getName());
        try {
            cbsRespMsg = (String) cbsDataFormat.toMessage(modelObjectsMap);
        } catch (Exception e) {
            throw new RuntimeException("��ɫƽ̨����ת��ʧ��.", e);
        }
        return cbsRespMsg;
    }


    //�������������������
    private byte[] marshalTpsRequestMsg(TpsTia5000 tpsTia) {
        Map<String, Object> modelObjectsMap = new HashMap<String, Object>();
        modelObjectsMap.put(tpsTia.getClass().getName(), tpsTia);
        FixedLengthTextDataFormat dataFormat = new FixedLengthTextDataFormat(tpsTia.getClass().getPackage().getName());
        byte[] buf;
        try {
            String sendMsg = (String) dataFormat.toMessage(modelObjectsMap);
            buf = generateTpsRequestHeader(sendMsg).getBytes(TPS_ENCODING);
        } catch (Exception e) {
            throw new RuntimeException("�����������Ĵ������");
        }

        return buf;
    }

    //������ɵ�������Ӧ����BEAN
    private TpsToa5000 unmarshalTpsResponseMsg(byte[] response) throws Exception {
        TpsToa5000 toa = new TpsToa5000();
        FixedLengthTextDataFormat dataFormat = new FixedLengthTextDataFormat(toa.getClass().getPackage().getName());
        toa = (TpsToa5000) dataFormat.fromMessage(response, "TpsToa5000");

        return toa;
    }

    //���ݵ�������������Ӧ����������ɫƽ̨��Ӧ����
    private String marshalCbsResponseMsg(CbsToa5000 cbsToa) {
        String cbsRespMsg = "";
        Map<String, Object> modelObjectsMap = new HashMap<String, Object>();
        modelObjectsMap.put(cbsToa.getClass().getName(), cbsToa);
        SeperatedTextDataFormat cbsDataFormat = new SeperatedTextDataFormat(cbsToa.getClass().getPackage().getName());
        try {
            cbsRespMsg = (String) cbsDataFormat.toMessage(modelObjectsMap);
        } catch (Exception e) {
            throw new RuntimeException("��ɫƽ̨����ת��ʧ��.", e);
        }
        return cbsRespMsg;
    }

    //=======���ݿ⴦��=================================================
    //����δ�����Ľɿ��¼
    private List<FsHdPaymentInfo> selectNotCanceledPaymentInfosFromDB(String startDate, String endDate) {
        SqlSessionFactory sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
        FsHdPaymentInfoMapper mapper;
        try (SqlSession session = sqlSessionFactory.openSession()) {
            mapper = session.getMapper(FsHdPaymentInfoMapper.class);
            FsHdPaymentInfoExample example = new FsHdPaymentInfoExample();
            example.createCriteria()
                    .andNotifyDateBetween(startDate, endDate)
                    .andLnkBillStatusNotEqualTo(BillStatus.CANCELED.getCode());
            return mapper.selectByExample(example);
        }
    }

    private List<FsHdPaymentItem> selectPaymentItemsFromDB(FsHdPaymentInfo paymentInfo) {
        SqlSessionFactory sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
        try (SqlSession session = sqlSessionFactory.openSession()) {
            FsHdPaymentItemExample example = new FsHdPaymentItemExample();
            example.createCriteria().andMainPkidEqualTo(paymentInfo.getPkid());
            FsHdPaymentItemMapper infoMapper = session.getMapper(FsHdPaymentItemMapper.class);
            return infoMapper.selectByExample(example);
        }
    }


    private void processTxn(FsHdPaymentInfo paymentInfo, Stdp10ProcessorRequest request) {
        SqlSessionFactory sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
        SqlSession session = sqlSessionFactory.openSession();
        try {
            paymentInfo.setFbChkFlag("1");
            FsHdPaymentInfoMapper infoMapper = session.getMapper(FsHdPaymentInfoMapper.class);
            infoMapper.updateByPrimaryKey(paymentInfo);
            session.commit();
        } catch (Exception e) {
            session.rollback();
            throw new RuntimeException("ҵ���߼�����ʧ�ܡ�", e);
        } finally {
            session.close();
        }
    }
}
