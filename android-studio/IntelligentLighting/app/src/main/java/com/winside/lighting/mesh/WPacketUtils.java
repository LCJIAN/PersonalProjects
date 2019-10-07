package com.winside.lighting.mesh;

import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class WPacketUtils {

    public static byte[] getDestAddress(Packet packet) {
        byte[] result = new byte[2];
        result[0] = packet.getData()[2];
        result[1] = packet.getData()[3];
        return result;
    }

    public static byte[] getMessage(Packet packet) {
        byte[] data = packet.getData();
        byte[] result = new byte[data.length - 6];
        System.arraycopy(data, 4, result, 0, result.length);
        return result;
    }

    public static Packet buildPacket(byte[] message) {
        Packet packet = new Packet();
        packet.setData(message);
        return packet;
    }


    private static final String IV_PARAMETER = "1234567890123456";

    public static byte[] sss(byte[] dest, byte[] message) {
        byte[] result = new byte[6 + message.length];
        result[0] = "{".getBytes()[0];
        result[1] = (byte) result.length;
        System.arraycopy(dest, 0, result, 2, dest.length);
        System.arraycopy(message, 0, result, 4, message.length);
        result[result.length - 2] = calculateCRC(dest, message);
        result[result.length - 1] = "}".getBytes()[0];
        return result;
    }

    public static byte calculateCRC(byte[] dest, byte[] message) {
        byte[] temp = new byte[dest.length + message.length];
        System.arraycopy(dest, 0, temp, 0, dest.length);
        System.arraycopy(message, 0, temp, dest.length, message.length);

        byte crc = 0x00;
        for (byte b : temp) {
            crc ^= b;
            for (int x = 8; x > 0; --x) { /* 下面这段计算过程与计算一个字节crc一样 */
                if ((crc & 0x80) != 0) {
                    crc = (byte) ((crc << 1) ^ 0x07); // 多项式 0x07
                } else {
                    crc = (byte) (crc << 1);
                }
            }
        }
        return crc;
    }

    public static List<byte[]> ss(byte[] mac, byte[] unicast, byte tid, byte[] data, int mtu) {
        byte[] key = generateAESKey(mac, unicast, tid);
        byte[] encryptedData = aesEncrypt(data, key);

        int maxFrameDataUnit = mtu - 4;
        byte frameCount = (byte) (encryptedData.length % maxFrameDataUnit == 0
                ? encryptedData.length / maxFrameDataUnit
                : encryptedData.length / maxFrameDataUnit + 1);

        List<byte[]> result = new ArrayList<>();
        byte index = 0;
        for (byte i = 0; i < frameCount; i++) {

            byte[] temp;
            if (i == frameCount - 1) {
                temp = new byte[maxFrameDataUnit];
            } else {
                temp = new byte[encryptedData.length % maxFrameDataUnit == 0 ? maxFrameDataUnit : encryptedData.length % maxFrameDataUnit];
            }

            for (int x = 0; x < temp.length; x++) {
                temp[x] = encryptedData[index];
                index++;
            }

            byte[] frame = new byte[4 + temp.length];
            frame[0] = (byte) frame.length;
            frame[1] = frameCount;
            frame[2] = i;
            frame[3] = tid;

            System.arraycopy(temp, 0, frame, 4, temp.length);
            result.add(frame);
        }

        return result;
    }

    public static byte[] generateAESKey(byte[] mac, byte[] unicast, byte tid) {
        byte[] result = new byte[(mac.length + unicast.length) * 2];
        System.arraycopy(mac, 0, result, 0, mac.length);
        System.arraycopy(unicast, 0, result, mac.length, unicast.length);
        System.arraycopy(mac, 0, result, mac.length + unicast.length, mac.length);
        System.arraycopy(unicast, 0, result, mac.length * 2 + unicast.length, unicast.length);
        result[result.length - 1] = tid;
        return result;
    }

    public static byte[] aesEncrypt(byte[] data, byte[] key) {
        byte[] encrypted = null;
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec sKeySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec iv = new IvParameterSpec(IV_PARAMETER.getBytes()); // 使用CBC模式，需要一个向量iv，可增加加密算法的强度
            cipher.init(Cipher.ENCRYPT_MODE, sKeySpec, iv);
            encrypted = cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encrypted;
    }
}
