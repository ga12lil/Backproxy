package org.niisva.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.niisva.handler.NodeHandlers.DataHandler;
import org.niisva.handler.NodeHandlers.ServiceHandler;
import org.niisva.util.ConnectionResolver;

@Slf4j
@RequiredArgsConstructor
public class NettyServer {
    private final int port;
    private final int backlogSize;
    private final ConnectionResolver connectionResolver;

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
                            Channel service = connectionResolver.tryGetServiceConnection(ch);
                            if (service == null) {
                                ch.pipeline().addLast(new ServiceHandler(connectionResolver)
                                );
                                connectionResolver.addNodeServiceConnection(ch);
                            }
                            else {
                                ch.pipeline().addLast(new DataHandler(connectionResolver)
                                );
                            }
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, backlogSize)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(port).sync();
            log.info("TCP Server started on port " + port);

            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
