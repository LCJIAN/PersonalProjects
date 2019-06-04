package org.gradle.mesh.udp;

import org.gradle.mesh.packet.Packet;
import org.gradle.mesh.packet.handler.RealUnPackerChain;
import org.gradle.mesh.packet.handler.UnPacker;
import org.gradle.mesh.udp.handler.UdpEncoder;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class UdpClient {

    private final int port;
    private final List<UnPacker> unPackers;
    private NioEventLoopGroup group;
    private Channel channel;

    public UdpClient(int port) {
        this.port = port;
        this.unPackers = new CopyOnWriteArrayList<>();
    }

    public void start() {
        group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    public void initChannel(Channel ch) {
                        ch.pipeline().addLast(new UdpEncoder(port, new RealUnPackerChain(null, unPackers, 0)));
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

    public void addUnPacker(UnPacker unPacker) {
        unPackers.add(unPacker);
    }

    public void removeUnPacker(UnPacker unPacker) {
        unPackers.remove(unPacker);
    }
}
