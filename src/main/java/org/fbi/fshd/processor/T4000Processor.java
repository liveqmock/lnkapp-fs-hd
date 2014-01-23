package org.fbi.fshd.processor;

import org.fbi.fshd.domain.cbs.T4000Request.CbsTia4000;
import org.fbi.fshd.domain.cbs.T4000Response.CbsToa4000;
import org.fbi.fshd.domain.tps.T4000Request.TpsTia4000;
import org.fbi.fshd.domain.tps.T4000Response.TpsToa4000;
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
 * ����¼�������Ϣ��ѯ
 */
public class T4000Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        CbsTia4000 cbsTia;
        try {
            cbsTia = unmarshalCbsRequestMsg(request.getRequestBody());
        } catch (Exception e) {
            logger.error("��ɫҵ��ƽ̨�����Ľ�������.", e);
            marshalAbnormalCbsResponse(TxnRtnCode.CBSMSG_UNMARSHAL_FAILED, null, response);
            return;
        }

        //������ͨѶ����
        TpsTia4000 tpsTia = new TpsTia4000();
        TpsToa4000 tpsToa;

        try {
            FbiBeanUtils.copyProperties(cbsTia, tpsTia);
            tpsTia.setFisCode(ProjectConfigManager.getInstance().getProperty("tps.fis.fiscode"));
            tpsTia.setTxnHdlCode("G");   //������ ���ݣ�G����ʾ������֤
            tpsTia.setFisActno(ProjectConfigManager.getInstance().getProperty("tps.fis.actno")); //����ר���˺�

            byte[] recvTpsBuf = processThirdPartyServer(marshalTpsRequestMsg(tpsTia), "4000");
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
    private CbsTia4000 unmarshalCbsRequestMsg(byte[] body) throws Exception {
        CbsTia4000 tia = new CbsTia4000();
        SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
        tia = (CbsTia4000) dataFormat.fromMessage(new String(body, "GBK"), "CbsTia4000");
        return tia;
    }

    //�������������������
    private byte[] marshalTpsRequestMsg(TpsTia4000 tpsTia) {
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
    private TpsToa4000 unmarshalTpsResponseMsg(byte[] response) throws Exception {
        TpsToa4000 toa = new TpsToa4000();
        FixedLengthTextDataFormat dataFormat = new FixedLengthTextDataFormat(toa.getClass().getPackage().getName());
        toa = (TpsToa4000) dataFormat.fromMessage(response, "TpsToa4000");

        return toa;
    }

    //���ݵ�������������Ӧ����������ɫƽ̨��Ӧ����
    private String marshalCbsResponseMsg(TpsToa4000 tpsToa) {
        CbsToa4000 cbsToa = new CbsToa4000();
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
