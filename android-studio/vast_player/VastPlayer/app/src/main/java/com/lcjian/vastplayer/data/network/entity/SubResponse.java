package com.lcjian.vastplayer.data.network.entity;

public class SubResponse<T> {

    public SubAction<T> sub;

    public static class SubAction<T> {

        public T subs;

    }
}
