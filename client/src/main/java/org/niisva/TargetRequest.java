package org.niisva;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.niisva.handler.MessageTargetServerHandler;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;
import org.niisva.handler.MessageProxyServerHandler;
import org.niisva.client.WorkClient;

import java.util.Objects;

@Slf4j
public class TargetRequest {
    public final String host;
    public final int port;
    public ByteBuf bytes;
    private Channel channel;
    private WorkClient parentClient;

    public TargetRequest(String host, int port, ByteBuf bytes, WorkClient parentClient) {
        this.host = host;
        this.port = port;
        this.bytes = bytes;
        this.parentClient = parentClient;
    }

    public void connect() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000); // 5 секунд
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new MessageTargetServerHandler(parentClient, TargetRequest.this));
                        }
                    });

            ChannelFuture future = bootstrap.connect(host, port).sync();

            channel = future.channel();

            SendingRequest();

            future.channel().closeFuture().sync();


        } finally {
            group.shutdownGracefully();
        }
    }

    public void SendingRequest()
    {
        channel.writeAndFlush(bytes);
        log.info("была сделана отправка");
    }
}
