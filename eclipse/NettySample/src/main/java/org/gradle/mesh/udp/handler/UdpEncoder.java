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

public class UdpEncoder extends MessageToMessageEncoder<Packet> {

    private final int port;

    private final UnPackerChain chain;

    public UdpEncoder(int port, UnPackerChain chain) {
        super();
        this.port = port;
        this.chain = chain;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet msg, List<Object> out) {
        String string = chain.proceed(msg);
        ByteBuf buf = ctx.alloc().buffer(string.length());
        buf.writeBytes(string.getBytes(CharsetUtil.UTF_8));
        out.add(new DatagramPacket(buf, new InetSocketAddress("255.255.255.255"/*DNS.resolve(msg.getPacketTo())*/, port)));
    }

}
