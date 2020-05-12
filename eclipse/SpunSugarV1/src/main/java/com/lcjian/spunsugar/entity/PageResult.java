package com.lcjian.spunsugar.entity;

import java.util.List;

import com.google.gson.annotations.Expose;

public class PageResult<T> {
    @Expose
    public Integer page;
    @Expose
    public Integer total_pages;
    @Expose
    public Integer total_results;
    @Expose
    public List<T> results;
}
