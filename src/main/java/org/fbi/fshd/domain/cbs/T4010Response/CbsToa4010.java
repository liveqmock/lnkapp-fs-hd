package org.fbi.fshd.domain.cbs.T4010Response;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

/**
 * Created by zhanrui on 14-1-16.
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsToa4010 {
    @DataField(seq = 1)
    private String rtnCode;      //����������
    @DataField(seq = 2)
    private String rtnMsg;       //�����������Ӧ����Ϣ
    @DataField(seq = 3)
    private String fisBizId;     //����ҵ��ID�� ԭ��ˮ�ţ���

    public String getFisBizId() {
        return fisBizId;
    }

    public void setFisBizId(String fisBizId) {
        this.fisBizId = fisBizId;
    }

    public String getRtnCode() {
        return rtnCode;
    }

    public void setRtnCode(String rtnCode) {
        this.rtnCode = rtnCode;
    }

    public String getRtnMsg() {
        return rtnMsg;
    }

    public void setRtnMsg(String rtnMsg) {
        this.rtnMsg = rtnMsg;
    }

    @Override
    public String toString() {
        return "CbsToa4010{" +
                "rtnCode='" + rtnCode + '\'' +
                ", rtnMsg='" + rtnMsg + '\'' +
                ", fisBizId='" + fisBizId + '\'' +
                '}';
    }
}
