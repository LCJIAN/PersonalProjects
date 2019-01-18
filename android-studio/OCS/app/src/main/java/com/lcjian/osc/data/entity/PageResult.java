package com.lcjian.osc.data.entity;

import java.util.List;

public class PageResult<T> {

    public Integer page_number;
    public Integer page_size;
    public Integer total_pages;
    public Integer total_elements;
    public List<T> elements;
}
