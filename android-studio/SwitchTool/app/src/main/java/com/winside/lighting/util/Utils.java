package com.winside.lighting.util;

import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
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

    public static byte[] hexStringToHexByte(String hexString) {
        //声明一个字节数组，其长度等于字符串长度的一半。
        byte[] buffer = new byte[hexString.length() / 2];
        for (int i = 0; i < buffer.length; i++) {
            //为字节数组的元素赋值。
            buffer[i] = (byte) Integer.parseInt(hexString.substring(i * 2, i * 2 + 2), 16);
        }
        //返回字节数组。
        return buffer;
    }

    public static String hexByteToHexString(byte[] ba) {
        StringBuilder result = new StringBuilder();
        for (byte b : ba) {
            result.append(byteToHexString(b));
        }
        return result.toString();
    }

    public static String byteToHexString(byte b) {
        String result = Integer.toHexString(0xff & b);
        if (result.length() < 2) {
            result = "0" + result;
        }
        return result;
    }

    public static String formatMac(String mac) {
        String[] ar = new String[mac.length() / 2];
        for (int i = 0; i < ar.length; i++) {
            ar[i] = mac.substring(i * 2, i * 2 + 2);
        }
        return TextUtils.join(":", ar).toUpperCase();
    }

    public static List<byte[]> convertAddresses(String addresses) {
        String[] a = addresses.split(",");
        List<byte[]> result = new ArrayList<>();
        for (String address : a) {
            result.add(Utils.short_to_bb_le((short) Integer.parseInt(address)));
        }
        return result;
    }

    public static byte[] short_to_bb_le(short aShort) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(aShort).array();
    }

    public static byte[] int_to_bb_le(int aInt) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(aInt).array();
    }

    public static int bb_to_int_le(byte[] byteBarray) {
        return ByteBuffer.wrap(byteBarray).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public static byte[] int_to_bb_be(int myInteger) {
        return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(myInteger).array();
    }

    public static int bb_to_int_be(byte[] byteBarray) {
        return ByteBuffer.wrap(byteBarray).order(ByteOrder.BIG_ENDIAN).getInt();
    }
}
