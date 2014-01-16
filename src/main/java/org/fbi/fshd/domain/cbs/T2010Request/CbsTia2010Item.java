package org.fbi.fshd.domain.cbs.T2010Request;


import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.OneToManySeperatedTextMessage;

import java.math.BigDecimal;

/**
 * Created by zhanrui on 14-1-16.
 */
@OneToManySeperatedTextMessage(separator = ",")
public class CbsTia2010Item {
    @DataField(seq = 1)
    private String vchClass;     //Ʊ������   1�����շ��վݡ���2��������ƾ֤��
    @DataField(seq = 2)
    private String vchNum;       //Ʊ�ݺ�
    @DataField(seq = 3)
    private BigDecimal txnAmt;    //��Ʊ���
    @DataField(seq = 4)
    private String vchSts;        //Ʊ��״̬   0��Ʊ����ȷ��1��Ʊ������


    public String getVchClass() {
        return vchClass;
    }

    public void setVchClass(String vchClass) {
        this.vchClass = vchClass;
    }

    public String getVchNum() {
        return vchNum;
    }

    public void setVchNum(String vchNum) {
        this.vchNum = vchNum;
    }

    public BigDecimal getTxnAmt() {
        return txnAmt;
    }

    public void setTxnAmt(BigDecimal txnAmt) {
        this.txnAmt = txnAmt;
    }

    public String getVchSts() {
        return vchSts;
    }

    public void setVchSts(String vchSts) {
        this.vchSts = vchSts;
    }

    @Override
    public String toString() {
        return "CbsTia2010Item{" +
                "vchClass='" + vchClass + '\'' +
                ", vchNum='" + vchNum + '\'' +
                ", txnAmt=" + txnAmt +
                ", vchSts='" + vchSts + '\'' +
                '}';
    }
}
