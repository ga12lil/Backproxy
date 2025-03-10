package org.niisva.client;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;
import org.niisva.handler.ConnectionHandler;
import org.niisva.handler.MessageHandler;
import java.util.Scanner;

@Slf4j
public class WorkClient {

    private final String host;
    private final int port;
    private Channel channel;

    public WorkClient(String host, int port) {
        this.host = host;
        this.port = port;
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
                            ch.pipeline().addLast(new ConnectionHandler());
                        }
                    });

            ChannelFuture future = bootstrap.connect(host, port).sync();

            log.info("Client connected to server at {}:{}", host, port);

            channel = future.channel();

            //startConsoleInput();

            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    /*
    private void startConsoleInput() {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String message = scanner.nextLine();

                if (channel != null && channel.isActive()) {
                    channel.writeAndFlush(message);
                    log.info("The client sent a message: {}", message);
                }
            }
        }).start();

    }
   */
}
