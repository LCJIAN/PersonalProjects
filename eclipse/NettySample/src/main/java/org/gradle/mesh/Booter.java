package org.gradle.mesh;

import org.gradle.mesh.packet.IQ.Type;
import org.gradle.mesh.packet.Message;
import org.gradle.mesh.packet.NodeIQ;
import org.gradle.mesh.udp.UdpClient;
import org.gradle.mesh.udp.UdpServer;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Random;

public class Booter {

    private UdpClient udpClient;
    private UdpServer udpServer;

    public Booter() {
        udpClient = new UdpClient(9999);
        udpServer = new UdpServer(9999);
    }

    public void boot() {
        try {
//            DNS.update();
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        udpServer.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        udpClient.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            Thread.sleep(1000);
            NodeIQ nodeIQ = new NodeIQ();
            nodeIQ.setNodeName("LCJIAN" + new Random().nextInt());
            nodeIQ.setNodeAvatar("http://www.baidu.com");
            nodeIQ.setPacketFrom(new NodeId("ss"));
            nodeIQ.setPacketTo(new NodeId("ff:ff:ff:ff:ff:ff"));
            nodeIQ.setNodeId("111");
            nodeIQ.setPacketId("sasasa");
            nodeIQ.setType(Type.GET);
            udpClient.send(nodeIQ);

            Message message = new Message();
            message.setContent("你好sb");
            message.setType(Message.Type.TEXT);
            message.setPacketFrom(new NodeId("ss"));
            message.setPacketTo(new NodeId("ff:ff:ff:ff:ff:ff"));
            message.setPacketId("sasasa");
            udpClient.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println(getIp());
        new Booter().boot();
    }

    /**
     * 多IP处理，可以得到最终ip
     */
    public static String getIp() {
        String localIp = null; // 本地IP，如果没有配置外网IP则返回它
        String netIp = null; // 外网IP
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip;
            boolean found = false;// 是否找到外网IP
            while (netInterfaces.hasMoreElements() && !found) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> address = ni.getInetAddresses();
                while (address.hasMoreElements()) {
                    ip = address.nextElement();
                    System.out.println(ni.getName() + ";" + ip.getHostAddress()
                            + ";ip.isSiteLocalAddress()=" + ip.isSiteLocalAddress()
                            + ";ip.isLoopbackAddress()=" + ip.isLoopbackAddress());
                    if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && !ip.getHostAddress().contains(":")) { // 外网IP
                        netIp = ip.getHostAddress();
                        found = true;
                        break;
                    } else if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress()
                            && !ip.getHostAddress().contains(":")) { // 内网IP
                        localIp = ip.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if (netIp != null && !"".equals(netIp)) {
            return netIp;
        } else {
            return localIp;
        }
    }
}
