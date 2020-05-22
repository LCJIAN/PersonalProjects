package com.org.firefighting.data.network.entity;

public class SearchRequest {

    public String index = "cq_search";
    public String type = "_doc";
    public Integer page;
    public Integer pager;
    public String category; // 资源目录:100000,文档:170010,标准:170020,服务:110000
    public String search;
}
