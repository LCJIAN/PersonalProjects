package org.gradle.mesh.udp;

import org.gradle.mesh.packet.Message;
import org.gradle.mesh.packet.NodeIQ;
import org.gradle.mesh.packet.Packet;
import org.gradle.mesh.packet.handler.RealUnPackerChain;
import org.gradle.mesh.udp.handler.UdpEncoder;

import java.util.Arrays;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class UdpClient {

    private final int port;
    private NioEventLoopGroup group;
    private Channel channel;

    public UdpClient(int port) {
        this.port = port;
    }

    public void start() {
        group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    public void initChannel(Channel ch) {
                        ch.pipeline().addLast(new UdpEncoder(port,
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
