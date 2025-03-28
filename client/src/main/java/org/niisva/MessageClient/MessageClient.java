package org.niisva.MessageClient;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.niisva.Node;
import org.niisva.TargetClient.TargetClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CompletableFuture;
@Slf4j
public class MessageClient {
    public String proxyServerHost;
    public int proxyServerPort;
    public Channel channel;
    public Node parentNode;
    //public HashMap<Pair, TargetClient> targetClients;
    public String targetServerHost;
    public int targetServerPort;
    public TargetClient targetClient;
    public int clientId;
    private final ExecutorService executorService;
    public CompletableFuture<Void> connectionFuture = new CompletableFuture<>();

    public MessageClient(String proxyServerHost, int proxyServerPort, int clientId, Node node)
    {
        this.proxyServerHost = proxyServerHost;
        this.proxyServerPort = proxyServerPort;
        this.clientId = clientId;
        this.parentNode = node;
        this.executorService = Executors.newCachedThreadPool();
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
                            ch.pipeline().addLast(new ProxyServerMessageHandler(MessageClient.this, parentNode, clientId));
                        }
                    });
            ChannelFuture future = bootstrap.connect(proxyServerHost, proxyServerPort).sync();
            channel = future.channel();

            log.info("Message client connected to proxy server:\r\n" +
                    "{}:{}\r\n" +
                    "client id: {}", proxyServerHost, proxyServerPort, clientId);
            future.channel().closeFuture().sync();
        }
        finally
        {
            group.shutdownGracefully();
        }
    }

    public void ConnectionToTargetAddress(String targetHost, int targetPort) {
        if (targetHost == targetServerHost && targetPort == targetServerPort)
        {
            log.error("Node already connected to target address: {}:{}", targetHost, targetPort);
            return;
            //TODO Отправлять какое-то сообщение прокси серверу если надо
        }
        targetClient = new TargetClient(targetHost, targetPort, this, parentNode);
        targetServerHost = targetHost;
        targetServerPort = targetPort;
        executorService.submit(() -> {
            try
            {
                targetClient.connect();
            }
            catch (InterruptedException e)
            {
                log.error("Connection to target: {}:{} failed", targetHost, targetPort);
                targetClient = null;
                targetServerHost = null;
                targetServerPort = -1;
                //TODO Отправлять какое-то сообщение прокси серверу если надо
            }
        });
    }

    public void SendMessageToTargetAddressViaTargetClient(ByteBuf msg)
    {
        connectionFuture.thenRun(() -> {
                targetClient.channel.writeAndFlush(msg);
                log.info("Message sent!");

        });
        log.info("MessageCLient send message by TargetClient to target server: {}:{} for client#{}",
                 targetServerHost, targetServerPort, clientId);
    }
}
