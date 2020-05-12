package org.gradle.mesh.packet.handler;

import org.gradle.mesh.packet.Packet;
import org.gradle.mesh.packet.handler.UnPacker.UnPackerChain;

import java.util.List;

public class RealUnPackerChain implements UnPackerChain {

    private final Packet content;
    private final List<UnPacker> unPackers;
    private final int index;

    public RealUnPackerChain(Packet content, List<UnPacker> unPackers, int index) {
        super();
        this.content = content;
        this.unPackers = unPackers;
        this.index = index;
    }

    @Override
    public Packet content() {
        return content;
    }

    @Override
    public String proceed(Packet content) {
        String s = null;
        if (unPackers.size() > index) {
            RealUnPackerChain realChain = new RealUnPackerChain(content, unPackers, index + 1);
            UnPacker unPacker = unPackers.get(index);
            s = unPacker.unPack(realChain);
        }
        return s;
    }
}
