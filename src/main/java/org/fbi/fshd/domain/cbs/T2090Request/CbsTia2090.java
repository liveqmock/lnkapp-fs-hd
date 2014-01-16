package org.fbi.fshd.domain.cbs.T2090Request;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

import java.math.BigDecimal;

@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsTia2090 {
    @DataField(seq = 1)
    private String fisBizId;      //����ҵ��ID��
    @DataField(seq = 2)
    private String instCode;      //��λ����
    @DataField(seq = 3)
    private BigDecimal payAmt;    //�ܽ��

    public String getFisBizId() {
        return fisBizId;
    }

    public void setFisBizId(String fisBizId) {
        this.fisBizId = fisBizId;
    }

    public String getInstCode() {
        return instCode;
    }

    public void setInstCode(String instCode) {
        this.instCode = instCode;
    }

    public BigDecimal getPayAmt() {
        return payAmt;
    }

    public void setPayAmt(BigDecimal payAmt) {
        this.payAmt = payAmt;
    }

    @Override
    public String toString() {
        return "CbsTia2090{" +
                "fisBizId='" + fisBizId + '\'' +
                ", instCode='" + instCode + '\'' +
                ", payAmt=" + payAmt +
                '}';
    }
}