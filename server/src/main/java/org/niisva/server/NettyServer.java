package org.niisva.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.buffer.ByteBuf;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.niisva.handler.ToConsoleOutputHandler;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;

@Slf4j
public class NettyServer {
    private final int port;
    private final int backlogSize;
    private Channel channel;

    public NettyServer(int port, int backlogSize) {
        this.port = port;
        this.backlogSize = backlogSize;
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline().addLast(new ToConsoleOutputHandler(NettyServer.this)
                            );
                            log.info("new connection with id: " + ch.id());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, backlogSize)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(port).sync();
            channel = f.channel();
            log.info("TCP Server started on port " + port);

            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
