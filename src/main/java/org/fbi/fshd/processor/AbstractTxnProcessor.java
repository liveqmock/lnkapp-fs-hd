package org.fbi.fshd.processor;

import org.apache.commons.lang.StringUtils;
import org.fbi.fshd.domain.cbs.T9999Response.TOA9999;
import org.fbi.fshd.enums.TxnRtnCode;
import org.fbi.fshd.helper.ProjectConfigManager;
import org.fbi.fshd.helper.TpsSocketClient;
import org.fbi.fshd.internal.AppActivator;
import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10Processor;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * User: zhanrui
 * Date: 2014-1-18
 */
public abstract class AbstractTxnProcessor extends Stdp10Processor {
    protected static String CONTEXT_TPS_AUTHCODE = "CONTEXT_TPS_AUTHCODE";
    protected static String TPS_ENCODING = "GBK";  //���������������뷽ʽ
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //protected static String tps_authcode = "";

    @Override
    public void service(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        String txnCode = request.getHeader("txnCode");
        String tellerId = request.getHeader("tellerId");
        if (StringUtils.isEmpty(tellerId)) {
            tellerId = "TELLERID";
        }

        try {
            MDC.put("txnCode", txnCode);
            MDC.put("tellerId", tellerId);
            logger.info("CBS Request:" + request.toString());
            doRequest(request, response);
            logger.info("CBS Response:" + response.toString());
        }catch (Exception e){
            response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_FAILED.getCode());
            throw new RuntimeException(e);
        } finally {
            MDC.remove("txnCode");
            MDC.remove("tellerId");
        }
    }

    abstract protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException;

    //���cbs���׳ɹ�����
    protected void marshalSuccessTxnCbsResponse(Stdp10ProcessorResponse response) {
        String msg = "���׳ɹ�";
        try {
            response.setHeader("rtnCode", TxnRtnCode.TXN_EXECUTE_SECCESS.getCode());
            response.setResponseBody(msg.getBytes(response.getCharacterEncoding()));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("�������", e);
        }
    }
    //���cbs�쳣����
    protected void marshalAbnormalCbsResponse(TxnRtnCode txnRtnCode, String errMsg, Stdp10ProcessorResponse response) {
        if (errMsg == null) {
            errMsg = txnRtnCode.getTitle();
        }
        String msg = getErrorRespMsgForStarring(errMsg);
        response.setHeader("rtnCode", txnRtnCode.getCode());
        try {
            response.setResponseBody(msg.getBytes(response.getCharacterEncoding()));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("�������", e);
        }
    }

    //��ͳһ�Ĵ�����Ӧ���� txtMsg
    private String getErrorRespMsgForStarring(String errMsg) {
        TOA9999 toa = new TOA9999();
        toa.setErrMsg(errMsg);
        String starringRespMsg;
        Map<String, Object> modelObjectsMap = new HashMap<String, Object>();
        modelObjectsMap.put(toa.getClass().getName(), toa);
        SeperatedTextDataFormat starringDataFormat = new SeperatedTextDataFormat(toa.getClass().getPackage().getName());
        try {
            starringRespMsg = (String) starringDataFormat.toMessage(modelObjectsMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return starringRespMsg;
    }

    //���ݵ������������ķ�����������ļ��л�ȡ��Ӧ����Ϣ
    protected String getTpsRtnErrorMsg(String rtnCode) {
        BundleContext bundleContext = AppActivator.getBundleContext();
        URL url = bundleContext.getBundle().getEntry("rtncode.properties");

        Properties props = new Properties();
        try {
            props.load(url.openConnection().getInputStream());
        } catch (Exception e) {
            throw new RuntimeException("�����������ļ���������", e);
        }
        String property = props.getProperty(rtnCode);
        if (property == null) {
            property = "δ�����Ӧ�Ĵ�����Ϣ(������:" + rtnCode + ")";
        }
        return property;
    }

    //������������ͨѶ����ͷ�ĳ��ȴ���
    protected String generateTpsRequestHeader(String sendMsg) throws UnsupportedEncodingException {
        String lenField = "" + (sendMsg.getBytes(TPS_ENCODING).length + 4);
        String rpad = "";
        for (int i = 0; i < 3 - lenField.length(); i++) {
            rpad += " ";
        }
        lenField = lenField + rpad;
        sendMsg = "0" + lenField + sendMsg;
        return sendMsg;
    }



    //�������������ɸ��ݽ��׺����ò�ͬ�ĳ�ʱʱ��
    protected byte[] processThirdPartyServer(byte[] sendTpsBuf, String txnCode) throws Exception {
        String servIp = ProjectConfigManager.getInstance().getProperty("tps.server.ip");
        int servPort = Integer.parseInt(ProjectConfigManager.getInstance().getProperty("tps.server.port"));
        TpsSocketClient client = new TpsSocketClient(servIp, servPort);

        String timeoutCfg = ProjectConfigManager.getInstance().getProperty("tps.server.timeout.txn." + txnCode);
        if (timeoutCfg != null) {
            int timeout = Integer.parseInt(timeoutCfg);
            client.setTimeout(timeout);
        } else {
            timeoutCfg = ProjectConfigManager.getInstance().getProperty("tps.server.timeout");
            if (timeoutCfg != null) {
                int timeout = Integer.parseInt(timeoutCfg);
                client.setTimeout(timeout);
            }
        }

        logger.info("TPS Request:" + new String(sendTpsBuf, TPS_ENCODING));
        byte[] rcvTpsBuf = client.call(sendTpsBuf);
        logger.info("TPS Response:" + new String(rcvTpsBuf, TPS_ENCODING));
        return rcvTpsBuf;
    }

}
