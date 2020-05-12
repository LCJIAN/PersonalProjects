package org.gradle.mesh.packet.handler;

import org.gradle.mesh.packet.Packet;
import org.gradle.mesh.packet.handler.Packer.PackerChain;

import java.util.List;

public class RealPackerChain implements PackerChain {

    private final String content;
    private final List<Packer> packers;
    private final int index;

    public RealPackerChain(String content, List<Packer> packers, int index) {
        super();
        this.content = content;
        this.packers = packers;
        this.index = index;
    }

    @Override
    public String content() {
        return content;
    }

    @Override
    public Packet proceed(String content) {
        Packet packet = null;
        if (packers.size() > index) {
            RealPackerChain realChain = new RealPackerChain(content, packers, index + 1);
            Packer packer = packers.get(index);
            packet = packer.pack(realChain);
        }
        return packet;
    }
}
