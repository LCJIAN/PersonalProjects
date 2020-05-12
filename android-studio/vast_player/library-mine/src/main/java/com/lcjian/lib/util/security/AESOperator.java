package com.lcjian.lib.util.security;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES 是一种可逆加密算法，对用户的敏感信息加密处理 对原始数据进行AES加密后，在进行Base64编码转化；
 */
public class AESOperator {
    private static AESOperator instance = null;
    /*
     * 加密用的Key 可以用26个字母和数字组成 此处使用AES-128-CBC加密模式，key需要为16位。
     */
    private String sKey = "TgOIiObDqDXLbExlbM0rwbwLZc/XxLSwCMYqPAL6Cbg=";
    private String ivParameter = "/PPwsH7HnWu27wxwRQQOfQ==";

    private AESOperator() {

    }

    public static AESOperator getInstance() {
        if (instance == null)
            instance = new AESOperator();
        return instance;
    }

    // 加密
    public String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] raw = Base64.decode(sKey, Base64.DEFAULT);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            IvParameterSpec iv = new IvParameterSpec(Base64.decode(ivParameter, Base64.DEFAULT));// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(data.getBytes("utf-8"));
            return Base64.encodeToString(encrypted, Base64.DEFAULT);// 此处使用BASE64做转码。
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    // 解密
    public String decrypt(String data) {
        try {
            byte[] raw = Base64.decode(sKey, Base64.DEFAULT);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(Base64.decode(ivParameter, Base64.DEFAULT));
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(Base64.decode(data, Base64.DEFAULT));
            return new String(original, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}