package org.gradle.mesh.udp.handler;

import java.util.List;

import org.gradle.mesh.packet.Packet;
import org.gradle.mesh.packet.handler.Packer.PackerChain;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.CharsetUtil;

public class BroadcastDecoder extends MessageToMessageDecoder<DatagramPacket> {

    private PackerChain chain;

    public BroadcastDecoder(PackerChain chain) {
        this.chain = chain;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {
        ByteBuf data = msg.content();
        String content = data.toString(CharsetUtil.UTF_8);
        Packet packet = chain.proceed(content);
        out.add(packet);
    }

}
