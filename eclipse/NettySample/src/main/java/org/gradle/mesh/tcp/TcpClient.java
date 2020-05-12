package org.gradle.mesh.tcp;

import org.gradle.mesh.tcp.handler.PacketHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

public class TcpClient {

    private String host;
    private int port;
    
    public TcpClient(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        
        try {
            Bootstrap b = new Bootstrap();
            b.group(bossGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                        ch.pipeline().addLast("frameEncoder", new LengthFieldPrepender(4));
                        ch.pipeline().addLast("handler", new PacketHandler(new ChannelManager().channelGroup()));
                    }
                })
                .option(ChannelOption.TCP_NODELAY, true);
            
            // 绑定端口，开始接收进来的连接
            ChannelFuture f = b.connect(host, port).sync();

            // 等待服务器  socket 关闭 。
            // 在这个例子中，这不会发生，但你可以优雅地关闭你的服务器。
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
        }
    }
}
