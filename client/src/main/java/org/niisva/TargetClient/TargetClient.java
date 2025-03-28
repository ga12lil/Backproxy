package org.niisva.TargetClient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.niisva.MessageClient.MessageClient;
import org.niisva.Node;
import io.netty.buffer.ByteBuf;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class TargetClient {
    public String host;
    public int port;
    public Channel channel;
    public Node parentNode;
    public MessageClient messageClient;
    public TargetClient(String host, int port, MessageClient messageClient, Node node)
    {
        this.host = host;
        this.port = port;
        this.messageClient = messageClient;
        this.parentNode = node;
    }

    public void connect() throws InterruptedException
    {
        EventLoopGroup group = new NioEventLoopGroup();
        try
        {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new TargetServerMessageHandler(messageClient, TargetClient.this));
                        }
                    });
            ChannelFuture future = bootstrap.connect(host, port).sync();
            channel = future.channel();

            messageClient.connectionFuture.complete(null);

            log.info("Target client connected to target server:\r\n" +
                    "{}:{}\r\n" +
                    "messageClient id: {}", host, port, messageClient.clientId);
            future.channel().closeFuture().sync();
            messageClient.connectionFuture = new CompletableFuture<>();
        }
        finally
        {
            group.shutdownGracefully();
        }
    }

    public void SendMessageToClientViaMessageHandler(ByteBuf msg)
    {
        if (messageClient == null)
        {
            log.info("MessageClient is null");
            return;
        }
        if (!messageClient.channel.isActive())
        {
            log.info("MessageClient channel was closed");
            return;
        }
        messageClient.channel.writeAndFlush(msg);
        log.info("TargetClient send message to client#{} from target server: {}:{}", messageClient.clientId, host, port);
    }
}
