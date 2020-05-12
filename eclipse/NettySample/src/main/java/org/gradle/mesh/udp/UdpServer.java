package org.gradle.mesh.udp;

import org.gradle.mesh.packet.Message;
import org.gradle.mesh.packet.NodeIQ;
import org.gradle.mesh.packet.handler.PacketCollector;
import org.gradle.mesh.packet.handler.RealPackerChain;
import org.gradle.mesh.udp.handler.UdpDecoder;
import org.gradle.mesh.udp.handler.UdpHandler;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class UdpServer {

    private final int port;
    private final List<PacketCollector> collectors;
    private Channel channel;

    public UdpServer(int port) {
        this.port = port;
        this.collectors = new CopyOnWriteArrayList<>();
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        public void initChannel(Channel ch) {
                            ch.pipeline()
                                    .addLast(new UdpDecoder(new RealPackerChain(null,
                                            Arrays.asList(new Message.MessagePacker(), new NodeIQ.NodeIQPacker()), 0)))
                                    .addLast(new UdpHandler(collectors));
                        }
                    }).option(ChannelOption.SO_BROADCAST, true);
            ChannelFuture f = bootstrap.bind(port).sync();
            channel = f.channel();
            channel.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public void stop() {
        channel.close();
    }
}
