package org.gradle.mesh.packet.handler;

import org.gradle.mesh.packet.Packet;

public abstract class PacketListener {

    private final PacketFilter packetFilter;

    public PacketListener(PacketFilter packetFilter) {
        this.packetFilter = packetFilter;
    }

    public final void processPacket(Packet packet) {
        if (packetFilter == null || packetFilter.accept(packet)) {
            onPacket(packet);
        }
    }

    public abstract void onPacket(Packet packet);
}
