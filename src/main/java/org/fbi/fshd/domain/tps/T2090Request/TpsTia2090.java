package org.fbi.fshd.domain.tps.T2090Request;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.FixedLengthTextMessage;

import java.math.BigDecimal;

@FixedLengthTextMessage(mainClass = true)
public class TpsTia2090 {
    @DataField(seq = 1, length = 1)
    private String fisCode;               //�����ֱ��� 4
    @DataField(seq = 2, length = 1)
    private String txnHdlCode;            //���״�����
    @DataField(seq = 3, length = 25)
    private String fisActno;              //����ר���ʺ�
    @DataField(seq = 4, length = 10)
    private String branchId;             //�������
    @DataField(seq = 5, length = 5)
    private String tlrId;                //����Ա����
    @DataField(seq = 6, length = 7)
    private String instCode;                //��λ����
    @DataField(seq = 7, length = 2)
    private String billType;              //֪ͨ����� 0:һ��֪ͨ�飻1�����س��ý���֪ͨ�飻�����ý�֪ͨ��������ã���ʱû�У�
    @DataField(seq = 8, length = 6)
    private String fisBatchSn;            //���κ�����Ϣ
    @DataField(seq = 9, length = 12)
    private String billId;                //�ɿ�֪ͨ���
    @DataField(seq = 10, length = 12)
    private BigDecimal payAmt;                 //�ܽ��
    @DataField(seq = 11, length = 1)
    private String outModeFlag;           //���ģʽ��ʶ


    public String getFisCode() {
        return fisCode;
    }

    public void setFisCode(String fisCode) {
        this.fisCode = fisCode;
    }

    public String getTxnHdlCode() {
        return txnHdlCode;
    }

    public void setTxnHdlCode(String txnHdlCode) {
        this.txnHdlCode = txnHdlCode;
    }

    public String getBillType() {
        return billType;
    }

    public void setBillType(String billType) {
        this.billType = billType;
    }

    public String getFisBatchSn() {
        return fisBatchSn;
    }

    public void setFisBatchSn(String fisBatchSn) {
        this.fisBatchSn = fisBatchSn;
    }

    public String getBillId() {
        return billId;
    }

    public void setBillId(String billId) {
        this.billId = billId;
    }

    public String getFisActno() {
        return fisActno;
    }

    public void setFisActno(String fisActno) {
        this.fisActno = fisActno;
    }

    public String getOutModeFlag() {
        return outModeFlag;
    }

    public void setOutModeFlag(String outModeFlag) {
        this.outModeFlag = outModeFlag;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getTlrId() {
        return tlrId;
    }

    public void setTlrId(String tlrId) {
        this.tlrId = tlrId;
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
        return "TpsTia1090{" +
                "fisCode='" + fisCode + '\'' +
                ", txnHdlCode='" + txnHdlCode + '\'' +
                ", fisActno='" + fisActno + '\'' +
                ", branchId='" + branchId + '\'' +
                ", tlrId='" + tlrId + '\'' +
                ", instCode='" + instCode + '\'' +
                ", billType='" + billType + '\'' +
                ", fisBatchSn='" + fisBatchSn + '\'' +
                ", billId='" + billId + '\'' +
                ", payAmt=" + payAmt +
                ", outModeFlag='" + outModeFlag + '\'' +
                '}';
    }
}