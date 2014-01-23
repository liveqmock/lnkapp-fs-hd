package org.fbi.fshd.domain.cbs.T4010Request;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

import java.math.BigDecimal;

/**
 * Created by zhanrui on 14-1-16.
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsTia4010 {
    @DataField(seq = 1)
    private String fisBizId;     //����ҵ��ID��
    @DataField(seq = 2)
    private String payerActno;    //�ɿ����˺�
    @DataField(seq = 3)
    private String payerName;     //�ɿ���
    @DataField(seq = 4)
    private String remark;        //��ע
    @DataField(seq = 5)
    private String payerBank;     //��������
    @DataField(seq = 6)
    private BigDecimal payAmt;    //���
    @DataField(seq = 7)
    private String notifyDate;    //֪ͨ����  ��ʽYYYYMMDD

    public String getPayerActno() {
        return payerActno;
    }

    public void setPayerActno(String payerActno) {
        this.payerActno = payerActno;
    }

    public String getPayerName() {
        return payerName;
    }

    public void setPayerName(String payerName) {
        this.payerName = payerName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getPayerBank() {
        return payerBank;
    }

    public void setPayerBank(String payerBank) {
        this.payerBank = payerBank;
    }

    public BigDecimal getPayAmt() {
        return payAmt;
    }

    public void setPayAmt(BigDecimal payAmt) {
        this.payAmt = payAmt;
    }

    public String getNotifyDate() {
        return notifyDate;
    }

    public void setNotifyDate(String notifyDate) {
        this.notifyDate = notifyDate;
    }

    public String getFisBizId() {
        return fisBizId;
    }

    public void setFisBizId(String fisBizId) {
        this.fisBizId = fisBizId;
    }

    @Override
    public String toString() {
        return "CbsTia4010{" +
                "fisBizId='" + fisBizId + '\'' +
                ", payerActno='" + payerActno + '\'' +
                ", payerName='" + payerName + '\'' +
                ", remark='" + remark + '\'' +
                ", payerBank='" + payerBank + '\'' +
                ", payAmt=" + payAmt +
                ", notifyDate='" + notifyDate + '\'' +
                '}';
    }
}
