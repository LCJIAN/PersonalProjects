package com.lcjian.osc.data.network.entity;

import java.io.Serializable;
import java.util.List;

public class GEOFences {

    public String state;
    public String deviceLat;
    public String deviceLng;
    public List<GEOFence> geofences;

    public static class GEOFence implements Serializable {

        /**
         * serialVersionUID
         */
        private static final long serialVersionUID = 1L;

        public String geofenceID;
        public String fenceName;
        public String lat;
        public String lng;
        public String radius;
        public String FenceType;
        public String Entry;
        public String Exit;
        public String createTime;
    }
}
