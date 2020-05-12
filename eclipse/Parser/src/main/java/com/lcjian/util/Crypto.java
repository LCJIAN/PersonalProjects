package com.lcjian.util;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * var get = function(a) {
 *   var b = CryptoJS.MD5(dfgsd);
 *   var c = CryptoJS.enc.Utf8.parse(b);
 *   var d = CryptoJS.enc.Utf8.parse(rgfgb);
 *   var e = CryptoJS.AES.encrypt(a, c, {
 *       iv: d,
 *       mode: CryptoJS.mode.CBC,
 *       padding: CryptoJS.pad.ZeroPadding
 *   });
 *   return e.toString()
 * }
 * 
 * @author Administrator
 *
 */
public class Crypto {

    public static String encrypt(String data) throws Exception {
        try {
            String key = MD5Utils.getMD532("contentWindowHig");
            String iv = "contentDocuments";
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            
            SecretKeySpec keyspec = new SecretKeySpec(zeroPadding(key.getBytes("utf-8"), 32), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes("utf-8"));
            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            byte[] encrypted = cipher.doFinal(zeroPadding(data.getBytes("utf-8")));
            return new String(Base64.encode(encrypted));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decrypt(String encrypted) throws Exception {
        try {
            String data = encrypted;
            String key = MD5Utils.getMD532("contentWindowHig");
            String iv = "contentDocuments";
            byte[] encrypted1 = Base64.decode(data);
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            
            SecretKeySpec keyspec = new SecretKeySpec(zeroPadding(key.getBytes("utf-8"), 32), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes("utf-8"));
            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
            byte[] original = cipher.doFinal(zeroPadding(encrypted1));
            String originalString = new String(trimZeros(original));
            return originalString;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static byte[] zeroPadding(byte[] text) {
        int count = text.length / 16;
        if (text.length % 16 != 0) {
            count++;
        }
        text = Arrays.copyOf(text, count * 16);
        return text;
    }

    private static byte[] zeroPadding(byte[] text, int length) {
        text = Arrays.copyOf(text, length);
        return text;
    }

    private static byte[] trimZeros(byte[] text) {
        int len = 0;
        for (int i = text.length - 1; i > 0; i--) {
            if (text[i] == 0) {
                len++;
            } else {
                break;
            }
        }
        if (len != 0) {
            text = Arrays.copyOf(text, text.length - len);
        }
        return text;
    }
}