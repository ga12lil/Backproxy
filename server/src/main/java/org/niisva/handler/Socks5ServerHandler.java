package org.niisva.handler;

import io.netty.channel.*;
import io.netty.handler.codec.socksx.v5.*;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Socks5ServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof Socks5InitialRequest) {
            // Отправляем клиенту, что аутентификация не требуется
            ctx.writeAndFlush(new DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH));
        } else if (msg instanceof Socks5CommandRequest request) {
            if (request.type() == Socks5CommandType.CONNECT) {
                handleConnect(ctx, request);
            } else {
                ctx.writeAndFlush(new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, request.dstAddrType()));
            }
        }
    }

    private void handleConnect(ChannelHandlerContext ctx, Socks5CommandRequest request) {
        log.info(request.toString());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
