package org.gradle.mesh.packet.handler;

import org.gradle.mesh.packet.Packet;

public interface UnPacker {

    String unPack(UnPackerChain chain);

    interface UnPackerChain {
        Packet content();

        String proceed(Packet packet);
    }
}
