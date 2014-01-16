package org.fbi.fshd.domain.cbs.T2090Response;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

/**
 * Created by zhanrui on 14-1-16.
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsToa2090 {
    @DataField(seq = 1)
    private String fisBizId;      //����ҵ��ID��

    public String getFisBizId() {
        return fisBizId;
    }

    public void setFisBizId(String fisBizId) {
        this.fisBizId = fisBizId;
    }

    @Override
    public String toString() {
        return "CbsToa2090{" +
                "fisBizId='" + fisBizId + '\'' +
                '}';
    }
}
