package org.niisva.client;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.niisva.TargetRequest;
import org.niisva.handler.MessageProxyServerHandler;

import java.util.ArrayList;

@Slf4j
public class WorkClient {

    public final String host;
    public final int port;
    public Channel channel;
    public ArrayList<TargetRequest> requests;

    public WorkClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.requests = new ArrayList<TargetRequest>();
    }

    public void connect() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new MessageProxyServerHandler(WorkClient.this));
                        }
                    });

            ChannelFuture future = bootstrap.connect(host, port).sync();

            log.info("Client connected to server at {}:{}", host, port);

            channel = future.channel();

            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
