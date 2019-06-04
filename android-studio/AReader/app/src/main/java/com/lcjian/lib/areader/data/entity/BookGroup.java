package com.lcjian.lib.areader.data.entity;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class BookGroup implements Displayable {

    @SerializedName("boy")
    public List<Book> boy;
    @SerializedName("boy_end")
    public List<Book> boyEnd;
    @SerializedName("girl")
    public List<Book> girl;
    @SerializedName("girl_end")
    public List<Book> girlEnd;
    @SerializedName("hot")
    public List<Book> hot;
    @SerializedName("slide")
    public List<SlideBook> slide;
    @SerializedName("today")
    public List<Book> today;

    public static class GroupStartItem implements Displayable {

        public String name;
        public List<Book> data;
        public int showCount;
        public int showMode;

        private int position;

        public GroupStartItem(String name, List<Book> data, int showCount, int showMode) {
            this.name = name;
            this.data = data;
            this.showCount = showCount;
            this.showMode = showMode;

            if (this.data != null && !this.data.isEmpty()) {
                for (Book book : this.data) {
                    book.showMode = showMode;
                }
            }
        }

        public List<Book> getShowData() {
            if (data == null) {
                return null;
            }
            List<Book> result = new ArrayList<>();
            for (int i = 0; i < showCount; i++) {
                result.add(data.get(position));
                position++;
                if (position == data.size()) {
                    position = 0;
                }
            }
            return result;
        }
    }

    public static class GroupEndItem implements Displayable {

        public String name;

        public GroupEndItem(String name) {
            this.name = name;
        }

    }
}
