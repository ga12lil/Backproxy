package org.niisva.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.socksx.v5.*;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@Slf4j
@RequiredArgsConstructor
public class Socks5ServerHandler extends ChannelInboundHandlerAdapter {

    private final ChannelGroup channels;
    private final HashMap<String, ByteBuf> targetAddresses;

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
        else {
            for (var ch : channels) {
                if (msg instanceof ByteBuf) {
                    ch.write(targetAddresses.get(ctx.channel().id().asLongText()));
                    ch.writeAndFlush(msg);
                }
                else {
                    log.warn("");
                }

            }
        }
    }

    private void handleConnect(ChannelHandlerContext ctx, Socks5CommandRequest request) {
        String result = request.dstAddr().length() + request.dstAddr() + request.dstPort();
        String id = ctx.channel().id().asLongText();
        targetAddresses.put(id, Unpooled.copiedBuffer(result, StandardCharsets.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
