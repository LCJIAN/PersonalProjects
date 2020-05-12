package org.gradle.mesh.udp.handler;

import org.gradle.mesh.packet.Message;
import org.gradle.mesh.packet.NodeIQ;
import org.gradle.mesh.packet.Packet;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

public class BroadcastHandler extends SimpleChannelInboundHandler<Packet> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet msg) throws Exception {
        try {
            if (msg instanceof NodeIQ) {
                
            } else if (msg instanceof Message) {
                
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
