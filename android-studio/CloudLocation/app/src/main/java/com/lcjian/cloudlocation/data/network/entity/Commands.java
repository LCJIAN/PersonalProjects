package com.lcjian.cloudlocation.data.network.entity;

import java.io.Serializable;
import java.util.List;

public class Commands {

    public String state;
    public String nowPage;
    public String resSize;
    public String deviceID;
    public String sn;
    public List<Command> commandArr;

    public static class Command implements Serializable {

        /**
         * serialVersionUID
         */
        private static final long serialVersionUID = 1L;

        public String id;
        public String commandName;
        public String isSend;
        public String sendDate;
        public String isResponse;
        public String responseText;
        public String responseDate;
    }
}
