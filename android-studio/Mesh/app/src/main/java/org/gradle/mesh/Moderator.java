package org.gradle.mesh;

import android.util.Log;

import org.gradle.mesh.packet.NodeIQ;
import org.gradle.mesh.roster.Roster;
import org.gradle.mesh.udp.UdpClient;
import org.gradle.mesh.udp.UdpServer;

import java.util.List;

public final class Moderator {

    private final UdpClient udpClient;
    private final UdpServer udpServer;

    public Moderator() {
        udpClient = new UdpClient(9999);
        udpServer = new UdpServer(9999);
    }

    public UdpServer getUdpServer() {
        return udpServer;
    }

    public UdpClient getUdpClient() {
        return udpClient;
    }

    public void start() {
        try {
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
            new Thread(new Runnable() {
                @Override
                public void run() {
                    List<NodeIQ> nodes = Roster.getInstanceFor(Moderator.this).getAllNeighbors();
                    for (NodeIQ nodeIQ : nodes) {
                        Log.d("mesh_fuck", "node:" + nodeIQ.getNodeName());
                    }
                }
            }).start();

//            Roster.removeInstanceFor(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
