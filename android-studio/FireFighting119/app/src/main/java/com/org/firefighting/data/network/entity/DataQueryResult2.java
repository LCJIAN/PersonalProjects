package com.org.firefighting.data.network.entity;

import java.util.List;
import java.util.Map;

public class DataQueryResult2 {

    public String code;
    public String message;
    public Result result;
    public List<Column> field;

    public static class Column {
        public String remarks;
        public String name;
        public Integer isSearch;
        public Integer isDisplay;
    }

    public static class Result {

        public Integer code;
        public Integer total;
        public Integer size;
        public Integer page;
        public String message;

        public List<Map<String, String>> result;
    }
}
