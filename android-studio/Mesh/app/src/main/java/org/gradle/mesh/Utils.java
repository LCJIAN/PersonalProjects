package org.gradle.mesh;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class Utils {

    public static InetAddress getLocalIp() {
        InetAddress localIp = null;
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip;
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> address = ni.getInetAddresses();
                while (address.hasMoreElements()) {
                    ip = address.nextElement();
                    if (ip.isSiteLocalAddress()
                            && !ip.isLoopbackAddress()
                            && !ip.getHostAddress().contains(":")) { // 过滤ipv6
                        localIp = ip;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return localIp;
    }

    public static String getMac(InetAddress ip) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            byte[] bytes = NetworkInterface.getByInetAddress(ip).getHardwareAddress();
            if (bytes != null) {
                for (int i = 0; i < bytes.length; i++) {
                    if (i != 0) {
                        stringBuilder.append(":");
                    }
                    String str = Integer.toHexString(bytes[i] & 0xff);
                    if (str.length() == 1) {
                        stringBuilder.append("0").append(str);
                    } else {
                        stringBuilder.append(str);
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static short getMask(InetAddress ip) {
        try {
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(ip);
            for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                if (!address.getAddress().getHostAddress().contains(":")) { // 过滤ipv6
                    return address.getNetworkPrefixLength();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int IpToCidr(String ip) {
        double sum = -2;
        String[] part = ip.split("\\.");
        for (String p : part) {
            sum += 256D - Double.parseDouble(p);
        }
        return 32 - (int) (Math.log(sum) / Math.log(2d));
    }

    public static long ipStringToLong(String ipStr) {
        String[] a = ipStr.split("\\.");
        return (Integer.parseInt(a[0]) * 16777216 + Integer.parseInt(a[1]) * 65536
                + Integer.parseInt(a[2]) * 256 + Integer.parseInt(a[3]));
    }

    public static String ipLongToString(long ip_long) {
        String ip = "";
        for (int k = 3; k > -1; k--) {
            ip = ip + ((ip_long >> k * 8) & 0xFF) + ".";
        }
        return ip.substring(0, ip.length() - 1);
    }

}
