package com.lcjian.mmt.data.network.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class QuotePrepare implements Serializable {

    @SerializedName("quoted")
    public TransRequest transRequest;
    @SerializedName("loadStore")
    public Store loadStore;
    @SerializedName("unloadeStore")
    public Store unloadedStore;
    @SerializedName("carlist")
    public List<Car> cars;

    public String id;
    public String taxRate;
    public List<TransQuoteForm> carsItem;

}
