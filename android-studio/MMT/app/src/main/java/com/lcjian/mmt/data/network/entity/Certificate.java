package com.lcjian.mmt.data.network.entity;

import java.io.Serializable;

public class Certificate implements Serializable {

    public String sortId;
    public String cerName;
    public String cerNo;
    public String picturePath;
    public String isForever; // 是否永久：0不是永久，1永久
    public Long expStart;
    public Long expEnd;
}
