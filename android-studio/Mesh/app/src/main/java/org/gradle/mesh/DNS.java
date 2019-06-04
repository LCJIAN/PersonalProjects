package org.gradle.mesh;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class DNS {

    private final static Map<String, String> macIpMap = new HashMap<>();
    private final static Map<String, String> ipMacMap = new HashMap<>();

    static {
        InetAddress inetAddress = Utils.getLocalIp();
        String ip = inetAddress.getHostAddress();
        String mac = Utils.getMac(inetAddress);
        macIpMap.put(mac, ip);
        ipMacMap.put(ip, mac);
    }

    public static String getIpByMac(String mac) {
        String ip = macIpMap.get(mac);
        if (ip == null || ip.isEmpty()) {
            update();
            ip = macIpMap.get(mac);
        }
        if (ip == null || ip.isEmpty()) {
            if ("ff:ff:ff:ff:ff:ff".equalsIgnoreCase(mac)) {
                return "255.255.255.255";
            }
        }
        return ip;
    }

    public static String getMacByIp(String ip) {
        String mac = ipMacMap.get(ip);
        if (mac == null || mac.isEmpty()) {
            update();
            mac = ipMacMap.get(ip);
        }
        return mac;
    }

    /**
     * Try to extract a hardware MAC address from a given IP address using the
     * ARP cache (/proc/net/arp).<br>
     * <br>
     * We assume that the file has this structure:<br>
     * <br>
     * IP address       HW type     Flags       HW address            Mask     Device
     * 192.168.18.11    0x1         0x2         00:04:20:06:55:1a     *        eth0
     * 192.168.18.36    0x1         0x2         00:22:43:ab:2a:5b     *        eth0
     */
    private static void load() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] array = line.split(" +");
                if (array.length >= 4) {
                    // Basic sanity check
                    String mac = array[3];
                    if (mac.matches("..:..:..:..:..:..") && !"00:00:00:00:00:00".equals(mac)) {
                        macIpMap.put(mac, array[0]);
                        ipMacMap.put(array[0], mac);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void update() {
        InetAddress localAddress = Utils.getLocalIp();
        short cidr = Utils.getMask(localAddress);
        long network_ip = Utils.ipStringToLong(localAddress.getHostAddress());
        int shift = (32 - cidr);

        long start;
        long end;
        if (cidr < 31) {
            start = (network_ip >> shift << shift) + 1;
            end = (start | ((1 << shift) - 1)) - 1;
        } else {
            start = (network_ip >> shift << shift);
            end = (start | ((1 << shift) - 1));
        }

        DatagramPacket dp = new DatagramPacket(new byte[0], 0, 0);
        DatagramSocket socket;
        try {
            socket = new DatagramSocket();
            int count = 0;
            for (long i = start; i <= end; i++) {
                if (count == 128) {
                    count = 0;
                    socket = new DatagramSocket();
                }
                String ipStr = Utils.ipLongToString(i);
                dp.setAddress(InetAddress.getByName(ipStr));
                socket.send(dp);
                count++;
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        load();
    }
}
