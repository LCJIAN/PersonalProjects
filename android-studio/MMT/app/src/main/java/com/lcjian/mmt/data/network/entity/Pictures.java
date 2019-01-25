package com.lcjian.mmt.data.network.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Pictures {

    @SerializedName("pictures")
    public List<Picture> list;

    public static class Picture {

        public String base64Text;
        public String captionText;
        public String rel;
        public String type;
    }
}
