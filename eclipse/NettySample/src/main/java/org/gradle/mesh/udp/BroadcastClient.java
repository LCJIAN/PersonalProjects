package org.gradle.mesh.udp;

import java.net.InetSocketAddress;
import java.util.Arrays;

import org.gradle.mesh.packet.Message;
import org.gradle.mesh.packet.NodeIQ;
import org.gradle.mesh.packet.Packet;
import org.gradle.mesh.packet.handler.RealUnPackerChain;
import org.gradle.mesh.udp.handler.BroadcastEncoder;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class BroadcastClient {

    private NioEventLoopGroup group;
    private Bootstrap bootstrap;
    private Channel channel;

    public BroadcastClient() {
        super();

        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioDatagramChannel.class).handler(new ChannelInitializer<Channel>() {
            @Override
            public void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(new BroadcastEncoder(new InetSocketAddress("255.255.255.255", 9999),
                        new RealUnPackerChain(null, Arrays.asList(new Message.MessageUnPacker(), new NodeIQ.NodeIQUnPacker()), 0)));
            }
        }).option(ChannelOption.SO_BROADCAST, true);
        channel = bootstrap.bind(0).syncUninterruptibly().channel();
    }

    public void send(Packet packet) {
        channel.writeAndFlush(packet);
    }

    public void stop() {
        channel.close();
        group.shutdownGracefully();
    }
}
