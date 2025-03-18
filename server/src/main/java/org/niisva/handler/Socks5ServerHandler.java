package org.niisva.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.handler.codec.socksx.v5.*;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.niisva.util.LoadBalancer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
public class Socks5ServerHandler extends ChannelInboundHandlerAdapter {

    private final LoadBalancer loadBalancer;
    private byte[] targetAddress = null;

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
        } else {
            if (msg instanceof ByteBuf msgBuf) {
//                byte[] bytes = new byte[msgBuf.readableBytes()];
//                msgBuf.getBytes(msgBuf.readerIndex(), bytes);

                ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
                ByteBuf bufAdr = allocator.buffer(); // Создает новый ByteBuf
//                ByteBuf buf = allocator.buffer();
                bufAdr.writeBytes(targetAddress);
//                buf.writeBytes(bytes);

                Channel ch = loadBalancer.getNodeChannelToSend(ctx.channel());
                ch.write(bufAdr);
//                ch.writeAndFlush(buf);
                ch.writeAndFlush(msgBuf);
            }
        }
    }

    private void handleConnect(ChannelHandlerContext ctx, Socks5CommandRequest request) {
        byte[] adr = request.dstAddr().getBytes(StandardCharsets.UTF_8);
        byte[] port = ByteBuffer.allocate(4).putInt(request.dstPort()).array();
        byte adrLen = (byte) adr.length;
        targetAddress = new byte[adrLen + 3];
        targetAddress[0] = adrLen;
        System.arraycopy(adr, 0,targetAddress, 1, adrLen);
        System.arraycopy(port, 2, targetAddress, adrLen + 1, 2);
        ctx.writeAndFlush(new DefaultSocks5CommandResponse(
                Socks5CommandStatus.SUCCESS,
                request.dstAddrType(),
                request.dstAddr(),
                request.dstPort()
        )).addListener(future -> {
            if (!future.isSuccess()) {
                System.err.println("Ошибка отправки: " + future.cause());
            }
            else {
                log.info("CONNECT RESPONSE WAS DELIVERED");
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
