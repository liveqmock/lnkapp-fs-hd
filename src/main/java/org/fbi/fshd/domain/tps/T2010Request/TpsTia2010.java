package org.fbi.fshd.domain.tps.T2010Request;

import org.fbi.fshd.domain.tps.T2000Response.TpsToa2000Item;
import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.FixedLengthTextMessage;
import org.fbi.linking.codec.dataformat.annotation.OneToMany;

import java.util.List;

/**
 * Created by zhanrui on 14-1-16.
 */
@FixedLengthTextMessage(mainClass = true)
public class TpsTia2010 {
    @DataField(seq = 1, length = 1)
    private String fisCode;               //�����ֱ��� 4
    @DataField(seq = 2, length = 1)
    private String txnHdlCode;            //���״�����
    @DataField(seq = 3, length = 10)
    private String fisBizId;             //����ҵ��ID��

    @DataField(seq = 4, length = 1)
    private String itemNum;

    @DataField(seq = 5, length = 22)
    @OneToMany(mappedTo = "org.fbi.fshd.domain.tps.T2000Response.TpsToa2000Item", totalNumberField = "itemNum")
    private List<TpsToa2000Item> items;

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

    public String getFisBizId() {
        return fisBizId;
    }

    public void setFisBizId(String fisBizId) {
        this.fisBizId = fisBizId;
    }

    public String getItemNum() {
        return itemNum;
    }

    public void setItemNum(String itemNum) {
        this.itemNum = itemNum;
    }

    public List<TpsToa2000Item> getItems() {
        return items;
    }

    public void setItems(List<TpsToa2000Item> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "TpsToa2000{" +
                "fisCode='" + fisCode + '\'' +
                ", txnHdlCode='" + txnHdlCode + '\'' +
                ", fisBizId='" + fisBizId + '\'' +
                ", itemNum='" + itemNum + '\'' +
                ", items=" + items +
                '}';
    }
}
