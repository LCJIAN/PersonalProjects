package com.lcjian.cloudlocation.data.network.entity;

import java.io.Serializable;
import java.util.List;

public class SubAccounts {

    public String state;
    public String userState;
    public List<SubAccount> userList;

    public static class SubAccount implements Serializable {

        /**
         * serialVersionUID
         */
        private static final long serialVersionUID = 1L;

        public String userID;
        public String parentID;
        public String userName;
        public String loginName;
    }
}
