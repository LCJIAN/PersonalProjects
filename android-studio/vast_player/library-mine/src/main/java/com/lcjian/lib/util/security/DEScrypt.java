package com.lcjian.lib.util.security;

import android.util.Base64;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class DEScrypt {
    /**
     * [加密]DES-->BASE64-->密文
     * <p>
     * [解密]BASE64->DES-->明文
     * <p>
     * 秘钥和向量要双方约定一致
     * <p>
     * DES加密的私钥，必须是8位长的字符串
     */
    private static final byte[] DESkey = Base64.decode("VW1nT3R2WVk=", Base64.DEFAULT);// 设置密钥
    private static final byte[] DESIV = Base64.decode("WXlIeEhsY0o=", Base64.DEFAULT);// 设置向量
    private static AlgorithmParameterSpec iv = null;// 加密算法的参数接口，IvParameterSpec是它的一个实现
    private static Key key = null;

    static {
        try {
            DESKeySpec keySpec = new DESKeySpec(DESkey);
            // 设置密钥参数
            iv = new IvParameterSpec(DESIV);// 设置向量
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");// 获得密钥工厂
            key = keyFactory.generateSecret(keySpec);// 得到密钥对象
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    public static String encode(String data) {
        String result = "";
        try {
            Cipher enCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");// 得到加密对象Cipher
            enCipher.init(Cipher.ENCRYPT_MODE, key, iv);// 设置工作模式为加密模式，给出密钥和向量
            byte[] pasByte = enCipher.doFinal(data.getBytes("utf-8"));
            result = Base64.encodeToString(pasByte, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String decode(String data) {
        String result = "";
        Cipher deCipher;
        try {
            deCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            deCipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] pasByte = deCipher.doFinal(Base64.decode(data, Base64.DEFAULT));
            result = new String(pasByte, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
