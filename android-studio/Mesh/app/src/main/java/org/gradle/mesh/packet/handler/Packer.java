package org.gradle.mesh.packet.handler;

import org.gradle.mesh.packet.Packet;

public interface Packer {

    Packet pack(PackerChain chain);

    interface PackerChain {
        String content();

        Packet proceed(String content);
    }
}
