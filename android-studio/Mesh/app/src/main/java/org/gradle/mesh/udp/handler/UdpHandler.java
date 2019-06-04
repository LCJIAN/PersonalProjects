package org.gradle.mesh.udp.handler;

import org.gradle.mesh.packet.Packet;
import org.gradle.mesh.packet.handler.PacketCollector;
import org.gradle.mesh.packet.handler.PacketListener;

import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

public class UdpHandler extends SimpleChannelInboundHandler<Packet> {

    private final List<PacketCollector> collectors;
    private final List<PacketListener> listeners;

    public UdpHandler(List<PacketCollector> collectors, List<PacketListener> listeners) {
        this.collectors = collectors;
        this.listeners = listeners;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet msg) {
        try {
            for (PacketListener listener : listeners) {
                listener.processPacket(msg);
            }
            for (PacketCollector collector : collectors) {
                collector.processPacket(msg);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
