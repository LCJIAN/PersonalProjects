package com.lcjian.cloudlocation.data.network.entity;

import java.io.Serializable;
import java.util.List;

public class Messages {

    public String state;
    public String nowPage;
    public String resSize;
    public List<Message> arr;

    public static class Message implements Serializable {

        /**
         * serialVersionUID
         */
        private static final long serialVersionUID = 1L;

        public String id;
        public String name;
        public String model;
        public String warn;
        public String deviceDate;
        public String createDate;
    }
}
