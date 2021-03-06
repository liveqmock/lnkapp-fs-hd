package org.fbi.fshd.domain.cbs.T6000Response;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.OneToMany;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

import java.util.List;

/**
 * Created by zhanrui on 14-1-16.
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsToa6000 {
    @DataField(seq = 1)
    private String rtnCode;
    @DataField(seq = 2)
    private String rtnMsg;
    @DataField(seq = 3)
    private String itemNum;
    @DataField(seq = 4)
    @OneToMany(mappedTo = "org.fbi.fshd.domain.cbs.T6000Response.CbsToa6000Item", totalNumberField = "itemNum")
    private List<CbsToa6000Item> items;

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

    public String getItemNum() {
        return itemNum;
    }

    public void setItemNum(String itemNum) {
        this.itemNum = itemNum;
    }

    public List<CbsToa6000Item> getItems() {
        return items;
    }

    public void setItems(List<CbsToa6000Item> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "CbsToa6000{" +
                "rtnCode='" + rtnCode + '\'' +
                ", rtnMsg='" + rtnMsg + '\'' +
                ", itemNum='" + itemNum + '\'' +
                ", items=" + items +
                '}';
    }
}
