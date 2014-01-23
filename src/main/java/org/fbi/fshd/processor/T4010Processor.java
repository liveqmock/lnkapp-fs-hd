package org.fbi.fshd.processor;

import org.fbi.fshd.domain.cbs.T4010Request.CbsTia4010;
import org.fbi.fshd.domain.cbs.T4010Response.CbsToa4010;
import org.fbi.fshd.domain.tps.T4010Request.TpsTia4010;
import org.fbi.fshd.domain.tps.T4010Response.TpsToa4010;
import org.fbi.fshd.enums.TxnRtnCode;
import org.fbi.fshd.helper.FbiBeanUtils;
import org.fbi.fshd.helper.ProjectConfigManager;
import org.fbi.linking.codec.dataformat.FixedLengthTextDataFormat;
import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhanrui on 14-1-20.
 * ����¼���������Ϣ��ѯ
 */
public class T4010Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        CbsTia4010 cbsTia;
        try {
            cbsTia = unmarshalCbsRequestMsg(request.getRequestBody());
        } catch (Exception e) {
            logger.error("��ɫҵ��ƽ̨�����Ľ�������.", e);
            marshalAbnormalCbsResponse(TxnRtnCode.CBSMSG_UNMARSHAL_FAILED, null, response);
            return;
        }

        //������ͨѶ����
        TpsTia4010 tpsTia = new TpsTia4010();
        TpsToa4010 tpsToa;

        try {
            FbiBeanUtils.copyProperties(cbsTia, tpsTia);
            tpsTia.setFisCode(ProjectConfigManager.getInstance().getProperty("tps.fis.fiscode"));
            tpsTia.setTxnHdlCode("I");   //������ ���ݣ�I����ʾ������֤
            tpsTia.setFisActno(ProjectConfigManager.getInstance().getProperty("tps.fis.actno")); //����ר���˺�

            byte[] recvTpsBuf = processThirdPartyServer(marshalTpsRequestMsg(tpsTia), "4010");
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
        response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_SECCESS.getCode());
        String cbsRespMsg = marshalCbsResponseMsg(tpsToa);
        response.setResponseBody(cbsRespMsg.getBytes(response.getCharacterEncoding()));
    }

    //�������CBS������BEAN
    private CbsTia4010 unmarshalCbsRequestMsg(byte[] body) throws Exception {
        CbsTia4010 tia = new CbsTia4010();
        SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
        tia = (CbsTia4010) dataFormat.fromMessage(new String(body, "GBK"), "CbsTia4010");
        return tia;
    }

    //�������������������
    private byte[] marshalTpsRequestMsg(TpsTia4010 tpsTia) {
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
    private TpsToa4010 unmarshalTpsResponseMsg(byte[] response) throws Exception {
        TpsToa4010 toa = new TpsToa4010();
        FixedLengthTextDataFormat dataFormat = new FixedLengthTextDataFormat(toa.getClass().getPackage().getName());
        toa = (TpsToa4010) dataFormat.fromMessage(response, "TpsToa4010");

        return toa;
    }

    //���ݵ�������������Ӧ����������ɫƽ̨��Ӧ����
    private String marshalCbsResponseMsg(TpsToa4010 tpsToa) {
        CbsToa4010 cbsToa = new CbsToa4010();
        FbiBeanUtils.copyProperties(tpsToa, cbsToa);
        cbsToa.setRtnMsg(getTpsRtnErrorMsg(tpsToa.getRtnCode()));

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

}
