package org.gradle.mesh.roster;

import org.gradle.mesh.DNS;
import org.gradle.mesh.Moderator;
import org.gradle.mesh.NodeId;
import org.gradle.mesh.Utils;
import org.gradle.mesh.packet.IQ;
import org.gradle.mesh.packet.NodeIQ;
import org.gradle.mesh.packet.Packet;
import org.gradle.mesh.packet.Presence;
import org.gradle.mesh.packet.handler.PacketCollector;
import org.gradle.mesh.packet.handler.PacketFilter;
import org.gradle.mesh.packet.handler.PacketListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class Roster {

    private static final Map<Moderator, Roster> INSTANCES = new WeakHashMap<>();
    private final Moderator moderator;
    private final PacketListener presenceListener;
    private final PacketListener nodeIQListener;
    private final NodeIQ.NodeIQPacker nodeIQPacker;
    private final NodeIQ.NodeIQUnPacker nodeIQUnPacker;
    private final Presence.PresencePacker presencePacker;
    private final Presence.PresenceUnPacker presenceUnPacker;

    public static synchronized Roster getInstanceFor(Moderator moderator) {
        Roster roster = INSTANCES.get(moderator);
        if (roster == null) {
            roster = new Roster(moderator);
            INSTANCES.put(moderator, roster);
        }
        return roster;
    }

    public static synchronized void removeInstanceFor(Moderator moderator) {
        Roster roster = INSTANCES.get(moderator);
        if (roster != null) {
            roster.destroy();
            INSTANCES.remove(moderator);
        }
    }

    private Roster(Moderator m) {
        this.moderator = m;
        this.nodeIQPacker = new NodeIQ.NodeIQPacker();
        this.nodeIQUnPacker = new NodeIQ.NodeIQUnPacker();
        this.presencePacker = new Presence.PresencePacker();
        this.presenceUnPacker = new Presence.PresenceUnPacker();
        this.presenceListener = new PacketListener(new PacketFilter() {
            @Override
            public boolean accept(Packet packet) {
                return packet instanceof Presence;
            }
        }) {
            @Override
            public void onPacket(Packet packet) {

            }
        };
        this.nodeIQListener = new PacketListener(new PacketFilter() {
            @Override
            public boolean accept(Packet packet) {
                return packet instanceof NodeIQ
                        && ((NodeIQ) packet).getType().toString().equals(IQ.Type.GET.toString());
            }
        }) {
            @Override
            public void onPacket(Packet packet) {
                NodeIQ nodeIQ = new NodeIQ();
                nodeIQ.setPacketId(packet.getPacketId());
                nodeIQ.setPacketTo(packet.getPacketFrom());
                nodeIQ.setPacketFrom(new NodeId(DNS.getMacByIp(Utils.getLocalIp().getHostAddress())));
                nodeIQ.setType(IQ.Type.RESULT);
                nodeIQ.setNodeName(Utils.getLocalIp().getHostName());
                nodeIQ.setNodeAvatar("hAAAd");
                moderator.getUdpClient().send(nodeIQ);
            }
        };

        moderator.getUdpClient().addUnPacker(nodeIQUnPacker);
        moderator.getUdpClient().addUnPacker(presenceUnPacker);
        moderator.getUdpServer().addPacker(nodeIQPacker);
        moderator.getUdpServer().addPacker(presencePacker);

        moderator.getUdpServer().addPacketListener(presenceListener);
        moderator.getUdpServer().addPacketListener(nodeIQListener);
    }

    private void destroy() {
        moderator.getUdpClient().removeUnPacker(nodeIQUnPacker);
        moderator.getUdpClient().removeUnPacker(presenceUnPacker);
        moderator.getUdpServer().removePacker(nodeIQPacker);
        moderator.getUdpServer().removePacker(presencePacker);

        moderator.getUdpServer().removePacketListener(presenceListener);
        moderator.getUdpServer().removePacketListener(nodeIQListener);
    }

    private List<NodeIQ> getNeighbors(NodeId nodeId) {
        final NodeIQ nodeIQ = new NodeIQ();
        nodeIQ.setType(IQ.Type.GET);
        nodeIQ.setPacketId(nodeIQ.getPacketId());
        nodeIQ.setPacketFrom(new NodeId(DNS.getMacByIp(Utils.getLocalIp().getHostAddress())));
        nodeIQ.setPacketTo(nodeId);

        PacketCollector packetCollector = new PacketCollector(new PacketFilter() {
            @Override
            public boolean accept(Packet packet) {
                return packet instanceof NodeIQ
                        && ((NodeIQ) packet).getType().toString().equals(IQ.Type.RESULT.toString())
                        && nodeIQ.getPacketId().equals(packet.getPacketId());
            }
        });

        moderator.getUdpServer().addPacketCollector(packetCollector);
        moderator.getUdpClient().send(nodeIQ);
        List<NodeIQ> result = new ArrayList<>();
        NodeIQ item = null;
        do {
            try {
                item = packetCollector.nextResult(4 * 1000);
                if (item != null) {
                    result.add(item);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (item != null);
        moderator.getUdpServer().removePacketCollector(packetCollector);
        return result;
    }

    public List<NodeIQ> getAllNeighbors() {
        return getNeighbors(new NodeId("ff:ff:ff:ff:ff:ff"));
    }

    public NodeIQ getNeighbor(NodeId nodeId) {
        List<NodeIQ> result = getNeighbors(nodeId);
        return result.isEmpty() ? null : result.get(0);
    }
}
