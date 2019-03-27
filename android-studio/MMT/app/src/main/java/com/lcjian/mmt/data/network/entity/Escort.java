package com.lcjian.mmt.data.network.entity;

import java.io.Serializable;
import java.util.List;

public class Escort implements Serializable {

    public String id;
    public String name;
    public String phone;
    public String mobile;

    public List<Certificate> certificates;
    public List<Image> images;
}
