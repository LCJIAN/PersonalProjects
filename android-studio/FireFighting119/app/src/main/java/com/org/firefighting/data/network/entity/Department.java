package com.org.firefighting.data.network.entity;

import java.util.List;

public class Department {

    public Integer id;
    public String name;
    public String code;
    public String category;
    public Integer level;
    public Boolean enabled;
    public Integer pid;
    public String canDelete;
    public Long createTime;
    public String label;
    public List<Department> children;
}
