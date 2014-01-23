package org.fbi.fshd.processor;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.fshd.domain.cbs.T2010Request.CbsTia2010;
import org.fbi.fshd.domain.cbs.T2010Request.CbsTia2010Item;
import org.fbi.fshd.domain.tps.T2010Request.TpsTia2010;
import org.fbi.fshd.domain.tps.T2010Request.TpsTia2010Item;
import org.fbi.fshd.domain.tps.T2010Response.TpsToa2010;
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
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by zhanrui on 14-1-20.
 * �ֹ�Ʊ�ɿ�ȷ�Ͻ���
 */
public class T2010Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        CbsTia2010 cbsTia;
        try {
            cbsTia = unmarshalCbsRequestMsg(request.getRequestBody());
        } catch (Exception e) {
            logger.error("��ɫҵ��ƽ̨�����Ľ�������.", e);
            marshalAbnormalCbsResponse(TxnRtnCode.CBSMSG_UNMARSHAL_FAILED, null, response);
            return;
        }

        //��鱾�����ݿ���Ϣ
        FsHdPaymentInfo paymentInfo_db = selectNotCanceledPaymentInfoFromDB(cbsTia.getFisBizId());
        if (paymentInfo_db != null) {
            String billStatus = paymentInfo_db.getLnkBillStatus();
            if (billStatus.equals(BillStatus.PAYOFF.getCode())) { //�ѽɿ�
                marshalAbnormalCbsResponse(TxnRtnCode.TXN_PAY_REPEATED, null, response);
                logger.info("===�˱ʽɿ�ѽɿ�.");
                return;
            }else if (!billStatus.equals(BillStatus.INIT.getCode())) {  //�ǳ�ʼ״̬
                marshalAbnormalCbsResponse(TxnRtnCode.TXN_EXECUTE_FAILED, "�˱ʽɿ״̬����", response);
                logger.info("===�˱ʽɿ״̬����.");
                return;
            }
        }

        //������ͨѶ����
        TpsTia2010 tpsTia = new TpsTia2010();
        TpsToa2010 tpsToa;

        try {
            FbiBeanUtils.copyProperties(cbsTia, tpsTia);
            tpsTia.setFisCode(ProjectConfigManager.getInstance().getProperty("tps.fis.fiscode"));
            tpsTia.setTxnHdlCode("2");   //������ ���ݣ�2����ʾҵ����ɡ����󱣴�

            List<TpsTia2010Item>  tpsTiaItems  = new ArrayList<>();
            for (CbsTia2010Item cbsTiaItem : cbsTia.getItems()) {
                TpsTia2010Item tpsTiaItem = new TpsTia2010Item();
                FbiBeanUtils.copyProperties(cbsTiaItem, tpsTiaItem);
                tpsTiaItems.add(tpsTiaItem);
            }
            tpsTia.setItems(tpsTiaItems);

            byte[] recvTpsBuf = processThirdPartyServer(marshalTpsRequestMsg(tpsTia), "2010");
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

        //��ɫƽ̨��Ӧ
        if ("0".equals(tpsToa.getRtnCode())) { //���׳ɹ�
            try {
                //TODO �жϷ��ص�FISBIZID�Ƿ��뷢����һ��

                processTxn(paymentInfo_db, request);
                marshalSuccessTxnCbsResponse(response);
            } catch (Exception e) {
                marshalAbnormalCbsResponse(TxnRtnCode.TXN_EXECUTE_FAILED, e.getMessage(), response);
                logger.error("ҵ����ʧ��.", e);
            }
        } else {  //����TPS���ش�����
            String errmsg = getTpsRtnErrorMsg(tpsToa.getRtnCode());
            marshalAbnormalCbsResponse(TxnRtnCode.TXN_EXECUTE_FAILED, errmsg, response);
        }
    }

    //�������CBS������BEAN
    private CbsTia2010 unmarshalCbsRequestMsg(byte[] body) throws Exception {
        CbsTia2010 tia = new CbsTia2010();
        SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
        tia = (CbsTia2010) dataFormat.fromMessage(new String(body, "GBK"), "CbsTia2010");
        return tia;
    }


    //�������������������
    private byte[] marshalTpsRequestMsg(TpsTia2010 tpsTia) {
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
    private TpsToa2010 unmarshalTpsResponseMsg(byte[] response) throws Exception {
        TpsToa2010 toa = new TpsToa2010();
        FixedLengthTextDataFormat dataFormat = new FixedLengthTextDataFormat(toa.getClass().getPackage().getName());
        toa = (TpsToa2010) dataFormat.fromMessage(response, "TpsToa2010");

        return toa;
    }


    //=======���ݿ⴦��=================================================
    //����δ�����Ľɿ��¼
    private FsHdPaymentInfo selectNotCanceledPaymentInfoFromDB(String fisBizId) {
        SqlSessionFactory sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
        FsHdPaymentInfoMapper mapper;
        try (SqlSession session = sqlSessionFactory.openSession()) {
            mapper = session.getMapper(FsHdPaymentInfoMapper.class);
            FsHdPaymentInfoExample example = new FsHdPaymentInfoExample();
            example.createCriteria()
                    .andFisBizIdEqualTo(fisBizId)
                    .andLnkBillStatusNotEqualTo(BillStatus.CANCELED.getCode());
            List<FsHdPaymentInfo> infos = mapper.selectByExample(example);
            if (infos.size() == 0) {
                return null;
            }
            if (infos.size() != 1) { //ͬһ���ɿ�ţ�δ�������ڱ���ֻ�ܴ���һ����¼
                throw new RuntimeException("��¼״̬����.");
            }
            return infos.get(0);
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
            Date date = new SimpleDateFormat("yyyyMMddHHmmss").parse(request.getHeader("txnTime"));
            paymentInfo.setBankindate(new SimpleDateFormat("yyyyMMdd").format(date));

            paymentInfo.setOperPayBankid(request.getHeader("branchId"));
            paymentInfo.setOperPayTlrid(request.getHeader("tellerId"));
            paymentInfo.setOperPayDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
            paymentInfo.setOperPayTime(new SimpleDateFormat("HHmmss").format(new Date()));
            paymentInfo.setOperPayHostsn(request.getHeader("serialNo"));

            paymentInfo.setHostBookFlag("1");
            paymentInfo.setHostChkFlag("0");
            paymentInfo.setFbBookFlag("1");
            paymentInfo.setFbChkFlag("0");

            paymentInfo.setHostAckFlag("0");
            paymentInfo.setLnkBillStatus(BillStatus.PAYOFF.getCode()); //�ѽɿ�
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
