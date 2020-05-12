package org.gradle.mesh.udp.handler;

import org.gradle.mesh.packet.Packet;
import org.gradle.mesh.packet.handler.PacketCollector;

import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

public class UdpHandler extends SimpleChannelInboundHandler<Packet> {

    private final List<PacketCollector> collectors;

    public UdpHandler(List<PacketCollector> collectors) {
        this.collectors = collectors;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet msg) {
        try {
            for (PacketCollector collector : collectors) {
                collector.processPacket(msg);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
