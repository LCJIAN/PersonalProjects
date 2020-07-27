package com.org.firefighting.data.network.entity;

import java.util.List;
import java.util.Map;

public class DataQueryResult {

    public Integer code;
    public Integer total;
    public Integer size;
    public Integer page;
    public String message;

    public List<Map<String, String>> data;
    public List<Column> columns;

    public static class Column {
        public String field;
        public Integer isSearch;
        public String name;
        public Integer isDisplay;
    }

}
