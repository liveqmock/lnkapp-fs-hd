package org.fbi.fshd.processor;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.fshd.domain.cbs.T3010Request.CbsTia3010;
import org.fbi.fshd.domain.tps.T3010Request.TpsTia3010;
import org.fbi.fshd.domain.tps.T3010Response.TpsToa3010;
import org.fbi.fshd.enums.BillStatus;
import org.fbi.fshd.enums.TxnRtnCode;
import org.fbi.fshd.helper.FbiBeanUtils;
import org.fbi.fshd.helper.MybatisFactory;
import org.fbi.fshd.helper.ProjectConfigManager;
import org.fbi.fshd.repository.dao.FsHdPaymentInfoMapper;
import org.fbi.fshd.repository.model.FsHdPaymentInfo;
import org.fbi.fshd.repository.model.FsHdPaymentInfoExample;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhanrui on 14-1-20.
 * ����֪ͨ�����ģʽ �ɿ�ȷ�Ͻ���
 */
public class T3010Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        CbsTia3010 cbsTia;
        try {
            cbsTia = unmarshalCbsRequestMsg(request.getRequestBody());
        } catch (Exception e) {
            logger.error("��ɫҵ��ƽ̨�����Ľ�������.", e);
            marshalAbnormalCbsResponse(TxnRtnCode.CBSMSG_UNMARSHAL_FAILED, null, response);
            return;
        }

        //��鱾�����ݿ���Ϣ
        FsHdPaymentInfo paymentInfo_db = selectNotCanceledPaymentInfoFromDB(cbsTia.getBillId());
        if (paymentInfo_db == null) {
            marshalAbnormalCbsResponse(TxnRtnCode.TXN_EXECUTE_FAILED, "��������ѯ����.", response);
            return;
        } else {
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
        TpsTia3010 tpsTia = new TpsTia3010();
        TpsToa3010 tpsToa;

        try {
            FbiBeanUtils.copyProperties(cbsTia, tpsTia);
            tpsTia.setFisCode(ProjectConfigManager.getInstance().getProperty("tps.fis.fiscode"));
            tpsTia.setTxnHdlCode("A");   //������ ���ݣ�A����ʾ�����տ���󱣴�
            tpsTia.setFisActno(ProjectConfigManager.getInstance().getProperty("tps.fis.actno")); //����ר���˺�
            //tpsTia.setVoucherType("01");     //֪ͨ������
            //tpsTia.setFisBatchSn("000001");   //���κ�����Ϣ
            tpsTia.setOutModeFlag("O");     //���ģʽ��ʶ
            tpsTia.setBranchId(request.getHeader("branchId"));
            tpsTia.setTlrId(request.getHeader("tellerId"));

            byte[] recvTpsBuf = processThirdPartyServer(marshalTpsRequestMsg(tpsTia), "3010");
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
                processTxn(cbsTia, paymentInfo_db, request);
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
    private CbsTia3010 unmarshalCbsRequestMsg(byte[] body) throws Exception {
        CbsTia3010 tia = new CbsTia3010();
        SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
        tia = (CbsTia3010) dataFormat.fromMessage(new String(body, "GBK"), "CbsTia3010");
        return tia;
    }

    //�������������������
    private byte[] marshalTpsRequestMsg(TpsTia3010 tpsTia) {
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
    private TpsToa3010 unmarshalTpsResponseMsg(byte[] response) throws Exception {
        TpsToa3010 toa = new TpsToa3010();
        FixedLengthTextDataFormat dataFormat = new FixedLengthTextDataFormat(toa.getClass().getPackage().getName());
        toa = (TpsToa3010) dataFormat.fromMessage(response, "TpsToa3010");

        return toa;
    }


    //=======���ݿ⴦��=================================================
    //����δ�����Ľɿ��¼
    private FsHdPaymentInfo selectNotCanceledPaymentInfoFromDB(String billId) {
        SqlSessionFactory sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
        FsHdPaymentInfoMapper mapper;
        try (SqlSession session = sqlSessionFactory.openSession()) {
            mapper = session.getMapper(FsHdPaymentInfoMapper.class);
            FsHdPaymentInfoExample example = new FsHdPaymentInfoExample();
            example.createCriteria()
                    .andBillIdEqualTo(billId)
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

    private void processTxn(CbsTia3010 cbsTia, FsHdPaymentInfo paymentInfo, Stdp10ProcessorRequest request) {
        SqlSessionFactory sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
        SqlSession session = sqlSessionFactory.openSession();
        try {
            Date date = new SimpleDateFormat("yyyyMMddHHmmss").parse(request.getHeader("txnTime"));
            paymentInfo.setBankindate(new SimpleDateFormat("yyyyMMdd").format(date));

            //�ɿ������Ϣ����ɫϵͳ���������ṩ
            paymentInfo.setVoucherType(cbsTia.getVoucherType());
            paymentInfo.setPayAmt(cbsTia.getPayAmt());

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
