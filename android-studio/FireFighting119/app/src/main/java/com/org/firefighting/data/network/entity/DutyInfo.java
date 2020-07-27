package com.org.firefighting.data.network.entity;

import java.util.List;
import java.util.Map;

public class DutyInfo {

    public Map<String, List<DutyItem>> userInfo;

    public static class DutyItem {

        public String type;
        public String name;
    }
}
