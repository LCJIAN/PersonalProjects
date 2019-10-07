package com.winside.lighting.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;

public class Utils {

    public static boolean testUrl(String urlString) {
        URL url;
        InputStream in = null;
        try {
            url = new URL(urlString);
            in = url.openStream();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static boolean testUrlWithTimeOut(String urlString, int timeOutMillSeconds) {
        URL url;
        URLConnection co;
        try {
            url = new URL(urlString);
            co = url.openConnection();
            co.setConnectTimeout(timeOutMillSeconds);
            co.connect();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static <T> T[] concat(List<T[]> param) {
        if (param.size() < 1)
            return null;
        if (param.size() < 2)
            return param.get(0);

        T[] first = param.get(0);
        List<T[]> rest = param.subList(1, param.size());

        int totalLength = first.length;
        for (T[] array : rest) {
            totalLength += array.length;

        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }

        return result;
    }

    public static byte[] concatForByte(List<byte[]> param) {
        if (param.size() < 1)
            return null;
        if (param.size() < 2)
            return param.get(0);

        byte[] first = param.get(0);
        List<byte[]> rest = param.subList(1, param.size());

        int totalLength = first.length;
        for (byte[] array : rest) {
            totalLength += array.length;

        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }

        return result;
    }

}
