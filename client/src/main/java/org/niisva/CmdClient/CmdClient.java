package org.niisva.CmdClient;

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
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

@Slf4j
public class CmdClient {
    public String host;
    public int port;
    public Channel channel;
    public Node parentNode;

    public CmdClient(String host, int port, Node node)
    {
        this.host = host;
        this.port = port;
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
                            ch.pipeline().addLast(new ProxyServerCmdHandler(CmdClient.this, parentNode));
                        }
                    });
            ChannelFuture future = bootstrap.connect(host, port).sync();
            channel = future.channel();

            log.info("Command client connected to proxy server:\r\n" +
                    "{}:{}", host, port);

            future.channel().closeFuture().sync();
        }
        finally
        {
            group.shutdownGracefully();
        }
    }

    public void ReadStartCommand(ByteBuf msg)
    {

        int typeCommand = msg.readByte();
        if (typeCommand == 1)
        {
            int clientId = msg.readUnsignedShort();
            parentNode.NodeConnectionToProxyServer(clientId);
        }
        else if (typeCommand == 2)
        {
            int clientId = msg.readUnsignedShort();
            int length = msg.readByte();
            byte[] targetAddress = new byte[length];
            msg.readBytes(targetAddress);
            int targetPort = msg.readUnsignedShort();
            parentNode.NodeConnectionToTargetAddress(new String(targetAddress, StandardCharsets.UTF_8),
                                                     targetPort, clientId);
        }
        else
        {
            log.error("Unknown command");
            //TODO Отправлять какое-то сообщение прокси серверу если надо
        }
    }

    public void OnFailConnectedToProxyServer(int clientId)
    {
        byte errorType = 1;

        ByteBuf buffer = Unpooled.buffer();
        buffer.writeByte(errorType);
        buffer.writeInt((short)clientId);

        channel.writeAndFlush(buffer);
    }

    public void OnAlreadyConnectedToProxyServerForClient(int clientId)
    {
        byte errorType = 3;

        ByteBuf buffer = Unpooled.buffer();
        buffer.writeByte(errorType);
        buffer.writeShort(clientId);

        channel.writeAndFlush(buffer);
    }

    public void OnFailConnectedToTargetAddress(int clientId, String targetHost, int targetPort)
    {
        byte errorType = 2;

        ByteBuf buffer = Unpooled.buffer();
        buffer.writeByte(errorType);
        buffer.writeShort((short)clientId);
        byte[] bytes = targetHost.getBytes();
        buffer.writeShort(bytes.length);
        buffer.writeBytes(bytes);
        buffer.writeShort(targetPort);

        channel.writeAndFlush(buffer);
    }

    public void OnAlreadyConnectedToTargetAddress(int clientId, String targetHost, int targetPort)
    {
        byte errorType = 4;

        ByteBuf buffer = Unpooled.buffer();
        buffer.writeByte(errorType);
        buffer.writeShort((short)clientId);
        byte[] bytes = targetHost.getBytes();
        buffer.writeShort(bytes.length);
        buffer.writeBytes(bytes);
        buffer.writeShort(targetPort);

        channel.writeAndFlush(buffer);
    }


}
