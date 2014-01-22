package org.fbi.fshd.domain.tps.T2000Request;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.FixedLengthTextMessage;
import org.fbi.linking.codec.dataformat.annotation.OneToMany;

import java.util.List;

@FixedLengthTextMessage(mainClass = true)
public class TpsTia2000 {
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
    @DataField(seq = 6, length = 6)
    private String fisBatchSn;            //���κ�����Ϣ
    @DataField(seq = 7, length = 12)
    private String billId;                //�ɿ�֪ͨ���
    @DataField(seq = 8, length = 7)
    private String instCode;              //��λ����
    @DataField(seq = 9, length = 30)
    private String payerName;              //�ɿ���

    @DataField(seq = 10, length = 1)
    private String itemNum;
    @DataField(seq = 11, length = 30)
    @OneToMany(mappedTo = "org.fbi.fshd.domain.tps.T2000Response.TpsTia2000Item", totalNumberField = "itemNum")
    private List<TpsTia2000Item> items;

    @DataField(seq = 12, length = 8)
    private String notifyDate;              //֪ͨ����
    @DataField(seq = 13, length = 8)
    private String latestDate;              //�������
    @DataField(seq = 14, length = 5)
    private String verifyNo;                //У����
    /*
        @DataField(seq = 15, length = 30)
        private String payerActno;              //�ɿ����˺�
        @DataField(seq = 16, length = 60)
        private String payerBank;               //�ɿ��˿�������
    */
    @DataField(seq = 15, length = 250)
    private String remark;                  //��ע
    @DataField(seq = 16, length = 2)
    private String voucherType;             //Ʊ������
    @DataField(seq = 17, length = 1)
    private String billType;                //֪ͨ�����
    @DataField(seq = 18, length = 1)
    private String outModeFlag;            //���ģʽ��ʶ

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

    public String getFisActno() {
        return fisActno;
    }

    public void setFisActno(String fisActno) {
        this.fisActno = fisActno;
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

    public String getInstCode() {
        return instCode;
    }

    public void setInstCode(String instCode) {
        this.instCode = instCode;
    }

    public String getPayerName() {
        return payerName;
    }

    public void setPayerName(String payerName) {
        this.payerName = payerName;
    }

    public String getItemNum() {
        return itemNum;
    }

    public void setItemNum(String itemNum) {
        this.itemNum = itemNum;
    }

    public List<TpsTia2000Item> getItems() {
        return items;
    }

    public void setItems(List<TpsTia2000Item> items) {
        this.items = items;
    }

    public String getNotifyDate() {
        return notifyDate;
    }

    public void setNotifyDate(String notifyDate) {
        this.notifyDate = notifyDate;
    }

    public String getLatestDate() {
        return latestDate;
    }

    public void setLatestDate(String latestDate) {
        this.latestDate = latestDate;
    }

    public String getVerifyNo() {
        return verifyNo;
    }

    public void setVerifyNo(String verifyNo) {
        this.verifyNo = verifyNo;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getVoucherType() {
        return voucherType;
    }

    public void setVoucherType(String voucherType) {
        this.voucherType = voucherType;
    }

    public String getBillType() {
        return billType;
    }

    public void setBillType(String billType) {
        this.billType = billType;
    }

    public String getOutModeFlag() {
        return outModeFlag;
    }

    public void setOutModeFlag(String outModeFlag) {
        this.outModeFlag = outModeFlag;
    }

    @Override
    public String toString() {
        return "TpsTia2000{" +
                "fisCode='" + fisCode + '\'' +
                ", txnHdlCode='" + txnHdlCode + '\'' +
                ", fisActno='" + fisActno + '\'' +
                ", branchId='" + branchId + '\'' +
                ", tlrId='" + tlrId + '\'' +
                ", fisBatchSn='" + fisBatchSn + '\'' +
                ", billId='" + billId + '\'' +
                ", instCode='" + instCode + '\'' +
                ", payerName='" + payerName + '\'' +
                ", itemNum='" + itemNum + '\'' +
                ", items=" + items +
                ", notifyDate='" + notifyDate + '\'' +
                ", latestDate='" + latestDate + '\'' +
                ", verifyNo='" + verifyNo + '\'' +
                ", remark='" + remark + '\'' +
                ", voucherType='" + voucherType + '\'' +
                ", billType='" + billType + '\'' +
                ", outModeFlag='" + outModeFlag + '\'' +
                '}';
    }
}