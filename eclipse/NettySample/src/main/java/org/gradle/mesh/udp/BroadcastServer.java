package org.gradle.mesh.udp;

import java.util.Arrays;

import org.gradle.mesh.packet.Message;
import org.gradle.mesh.packet.NodeIQ;
import org.gradle.mesh.packet.handler.RealPackerChain;
import org.gradle.mesh.udp.handler.BroadcastDecoder;
import org.gradle.mesh.udp.handler.BroadcastHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class BroadcastServer {

    private int port;

    public BroadcastServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioDatagramChannel.class).handler(new ChannelInitializer<Channel>() {
                @Override
                public void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(new BroadcastDecoder(new RealPackerChain(null,
                            Arrays.asList(new Message.MessagePacker(), new NodeIQ.NodeIQPacker()), 0)));
                    ch.pipeline().addLast(new BroadcastHandler());
                }
            }).option(ChannelOption.SO_BROADCAST, true);

            // 绑定端口，开始接收进来的连接
            ChannelFuture f = bootstrap.bind(port).sync();

            // 等待服务器 socket 关闭 。
            // 在这个例子中，这不会发生，但你可以优雅地关闭你的服务器。
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
