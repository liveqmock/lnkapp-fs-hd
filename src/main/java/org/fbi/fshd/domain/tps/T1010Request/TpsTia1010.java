package org.fbi.fshd.domain.tps.T1010Request;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.FixedLengthTextMessage;

@FixedLengthTextMessage(mainClass = true)
public class TpsTia1010 {
    @DataField(seq = 1, length = 1)
    private String fisCode;               //�����ֱ��� 4
    @DataField(seq = 2, length = 1)
    private String txnHdlCode;            //���״�����
    @DataField(seq = 3, length = 10)
    private String branchId;              //�������
    @DataField(seq = 4, length = 5)
    private String tlrId;                 //����Ա����
    @DataField(seq = 5, length = 2)
    private String voucherType;           //Ʊ������
    @DataField(seq = 6, length = 6)
    private String fisBatchSn;            //���κ�����Ϣ
    @DataField(seq = 7, length = 12)
    private String billId;                //�ɿ�֪ͨ���
    @DataField(seq = 8, length = 25)
    private String fisActno;              //����ר���ʺ�
    @DataField(seq = 9, length = 1)
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

    public String getVoucherType() {
        return voucherType;
    }

    public void setVoucherType(String voucherType) {
        this.voucherType = voucherType;
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

    @Override
    public String toString() {
        return "TpsTia1010{" +
                "fisCode='" + fisCode + '\'' +
                ", txnHdlCode='" + txnHdlCode + '\'' +
                ", branchId='" + branchId + '\'' +
                ", tlrId='" + tlrId + '\'' +
                ", voucherType='" + voucherType + '\'' +
                ", fisBatchSn='" + fisBatchSn + '\'' +
                ", billId='" + billId + '\'' +
                ", fisActno='" + fisActno + '\'' +
                ", outModeFlag='" + outModeFlag + '\'' +
                '}';
    }
}