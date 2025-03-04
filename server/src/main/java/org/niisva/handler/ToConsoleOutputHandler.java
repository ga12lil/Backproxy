package org.niisva.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import io.netty.buffer.ByteBuf;

@Slf4j
public class ToConsoleOutputHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf byteBuf = (ByteBuf) msg;
            // Читаем два байта из ByteBuf, которые представляют порт
            int receivedPort = byteBuf.readUnsignedShort();  // Читаем два байта как беззнаковое число (порт)
            log.info("Received port: {}", receivedPort);
        } else {
            log.warn("Received unexpected message type: {}", msg.getClass().getSimpleName());
        }
        //log.info("Received from " + ctx.channel().id() +" mes: " + (String) msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
