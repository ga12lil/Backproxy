package org.niisva.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

@Slf4j
public class ConnectionHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Client connected: {}", ctx.channel().remoteAddress());

        int portToSend = 10080;
        ByteBuf buffer = Unpooled.buffer(2);
        buffer.writeShort(portToSend);

        ctx.writeAndFlush(buffer);
        log.info("Sent port {} as bytes", portToSend);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Exception in ConnectionHandler", cause);
        ctx.close();
    }
}
