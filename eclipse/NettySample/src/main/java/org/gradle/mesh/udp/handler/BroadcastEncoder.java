package org.gradle.mesh.udp.handler;

import java.net.InetSocketAddress;
import java.util.List;

import org.gradle.mesh.packet.Packet;
import org.gradle.mesh.packet.handler.UnPacker.UnPackerChain;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.CharsetUtil;

public class BroadcastEncoder extends MessageToMessageEncoder<Packet> {

    private InetSocketAddress mRemoteAddress;

    private UnPackerChain chain;

    public BroadcastEncoder(InetSocketAddress remoteAddress, UnPackerChain chain) {
        super();
        this.mRemoteAddress = remoteAddress;
        this.chain = chain;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet msg, List<Object> out) throws Exception {
        String string = chain.proceed(msg);
        ByteBuf buf = ctx.alloc().buffer(string.length());
        buf.writeBytes(string.getBytes(CharsetUtil.UTF_8));
        out.add(new DatagramPacket(buf, mRemoteAddress));
    }

}
