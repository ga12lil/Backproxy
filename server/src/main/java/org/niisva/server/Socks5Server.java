package org.niisva.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.socksx.v5.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.niisva.handler.Socks5ServerHandler;

@Slf4j
@RequiredArgsConstructor
public class Socks5Server {

    private final int port;
    private final int backlogSize;
    private final ChannelGroup channels;
    private final ChannelGroup socks5channels;

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline().addLast(
                                    new Socks5InitialRequestDecoder(),
                                    new Socks5CommandRequestDecoder(),
                                    new Socks5ServerHandler()
                            );
                            socks5channels.add(ch);
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, backlogSize)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(port).sync();
            log.info("SOCKS5 server started on port " + port);
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
