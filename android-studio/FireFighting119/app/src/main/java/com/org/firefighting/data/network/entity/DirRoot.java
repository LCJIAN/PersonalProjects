package com.org.firefighting.data.network.entity;

import java.util.List;

public class DirRoot {

    public Integer id;
    public Integer pid;
    public String label;
    public String value;
    public String sort;
    public String remarks;
    public List<Dir> children;
}
