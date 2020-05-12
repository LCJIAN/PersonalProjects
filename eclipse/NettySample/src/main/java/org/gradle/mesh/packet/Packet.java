package org.gradle.mesh.packet;

import org.gradle.mesh.NodeId;

import java.util.UUID;

public abstract class Packet {

    private String packetId;
    private NodeId packetTo;
    private NodeId packetFrom;

    public String getPacketId() {
        if (packetId == null) {
            packetId = UUID.randomUUID().toString();
        }
        return packetId;
    }

    public void setPacketId(String packetId) {
        this.packetId = packetId;
    }

    public NodeId getPacketTo() {
        return packetTo;
    }

    public void setPacketTo(NodeId packetTo) {
        this.packetTo = packetTo;
    }

    public NodeId getPacketFrom() {
        return packetFrom;
    }

    public void setPacketFrom(NodeId packetFrom) {
        this.packetFrom = packetFrom;
    }
}
