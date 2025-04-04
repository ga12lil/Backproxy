package org.niisva.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.handler.codec.socksx.v5.*;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.niisva.util.ConnectionResolver;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class Socks5ServerHandler extends ChannelInboundHandlerAdapter {

    private final ConnectionResolver connectionResolver;
    private final CompletableFuture<ByteBuf> nodeDataChannelFuture = new CompletableFuture<>();
    //private byte[] targetAddress = null;

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
                Channel ch = connectionResolver.getDataChannel(ctx.channel());
                if (ch == null) { //если запрос пришел раньше чем нода выделила подключение для этого клиента
                    nodeDataChannelFuture.thenAccept(idData -> {
                        Channel toSend = connectionResolver.getDataChannel(ctx.channel());
                        log.info("ready to send");
                        toSend.writeAndFlush(msgBuf);
                    });
                }
                else {
                    ch.writeAndFlush(msgBuf);
                }
            }
        }
    }

    private void handleConnect(ChannelHandlerContext ctx, Socks5CommandRequest request) {
        Channel ch = connectionResolver.getServiceChannel(ctx.channel());
        ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;

        byte msgType = (byte) 2;
        ByteBuf msgTypeBuf = allocator.buffer();
        msgTypeBuf.writeByte(msgType);
        ch.write(msgTypeBuf);

        int id = connectionResolver.getIdForClient(ctx.channel());
        byte[] byteId = ByteBuffer.allocate(4).putInt(id).array();
        ByteBuf idBuf = allocator.buffer();
        idBuf.writeBytes(Arrays.copyOfRange(byteId, 2, byteId.length));
        ch.write(idBuf);

        byte[] adr = request.dstAddr().getBytes(StandardCharsets.UTF_8);
        byte[] port = ByteBuffer.allocate(4).putInt(request.dstPort()).array();
        byte adrLen = (byte) adr.length;
        byte [] targetAddress = new byte[adrLen + 3];
        targetAddress[0] = adrLen;
        System.arraycopy(adr, 0,targetAddress, 1, adrLen);
        System.arraycopy(port, 2, targetAddress, adrLen + 1, 2);
        ByteBuf adrBuf = allocator.buffer();
        adrBuf.writeBytes(targetAddress);
        ch.writeAndFlush(adrBuf);

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

    public void setFutureComplete(ByteBuf data) {
        nodeDataChannelFuture.complete(data);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        connectionResolver.disconnectClient(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
