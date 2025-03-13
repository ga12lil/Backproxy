package org.niisva.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.netty.buffer.ByteBuf;

@Slf4j
@RequiredArgsConstructor
public class ClientHandler extends ChannelInboundHandlerAdapter {

    private final ChannelGroup socks5channels;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("channelRead called");
        if (msg instanceof ByteBuf msgBuf) {
            byte[] bytes = new byte[msgBuf.readableBytes()];
            msgBuf.getBytes(msgBuf.readerIndex(), bytes);
            for (var ch : socks5channels) {
                ch.writeAndFlush(bytes);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
