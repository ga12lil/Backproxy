package org.niisva.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import io.netty.buffer.ByteBuf;
import org.niisva.server.NettyServer;

import java.nio.charset.StandardCharsets;

@Slf4j
public class ToConsoleOutputHandler extends ChannelInboundHandlerAdapter {

    public NettyServer parentServer;

    public ToConsoleOutputHandler(NettyServer parentServer)
    {
        this.parentServer = parentServer;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("Information: request received");
        if (msg instanceof ByteBuf) {
            ByteBuf buffer = (ByteBuf) msg;
            byte[] bytes = new byte[buffer.readableBytes()];
            buffer.readBytes(bytes);
            log.info("Answer: {}", new String(bytes, StandardCharsets.UTF_8));
        }
        else
        {
            log.info("Unexpected type");
        }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        log.info("Client connected: {}", ctx.channel().remoteAddress());
        String host1 = "example.com";
        short port1 = 80;
        ByteBuf buffer = Unpooled.buffer();
        byte[] hostBytes = host1.getBytes(StandardCharsets.UTF_8);

        buffer.writeByte(hostBytes.length);
        buffer.writeBytes(hostBytes);
        buffer.writeShort(port1);

        String httpRequest = "GET / HTTP/1.1\r\n" +
                "Host: " + host1 + "\r\n" +
                "Connection: close\r\n" +
                "User-Agent: curl/8.9.1\r\n" +
                "\r\n";

        buffer.writeBytes(httpRequest.getBytes());
        log.info("Server send request to: {}:{}", host1, port1);
        ctx.writeAndFlush(buffer);


        /*
        log.info("Client connected: {}", ctx.channel().remoteAddress());
        String host1 = "smtp.mail.ru";
        short port1 = 587;
        ByteBuf buffer = Unpooled.buffer();
        byte[] hostBytes = host1.getBytes(StandardCharsets.UTF_8);

        buffer.writeByte(hostBytes.length);
        buffer.writeBytes(hostBytes);
        buffer.writeShort(port1);

        String smtpRequest = "EHLO example.com\r\n";

        buffer.writeBytes(smtpRequest.getBytes(StandardCharsets.UTF_8));

        log.info("Server send request to: {}:{}", host1, port1);
        ctx.writeAndFlush(buffer);
        */
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
