package org.fbi.fshd.domain.cbs.T2010Request;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.OneToMany;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

import java.util.List;

/**
 * Created by zhanrui on 14-1-16.
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsTia2010 {
    @DataField(seq = 1)
    private String fisBizId;      //����ҵ��ID��
    @DataField(seq = 2)
    private String instName;      //��λ

    @DataField(seq = 11)
    private String itemNum;

    @DataField(seq = 12)
    @OneToMany(mappedTo = "org.fbi.fshd.domain.cbs.T2000Response.CbsTia2010Item", totalNumberField = "itemNum")
    private List<CbsTia2010Item> items;

}
