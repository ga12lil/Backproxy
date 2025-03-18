package org.niisva.handler;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.netty.buffer.ByteBuf;
import org.niisva.util.LoadBalancer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
public class ClientHandler extends ChannelInboundHandlerAdapter {

    private final LoadBalancer loadBalancer;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("channelRead called");
        if (msg instanceof ByteBuf msgBuf) {
            byte[] bytes = new byte[msgBuf.readableBytes()];
            msgBuf.getBytes(msgBuf.readerIndex(), bytes);
            byte[] idBytes = Arrays.copyOfRange(bytes, 0, 2);
            byte[] data = Arrays.copyOfRange(bytes, 2, bytes.length);
            ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;

            int id = ByteBuffer.wrap(idBytes)
                    .order(ByteOrder.BIG_ENDIAN)
                    .getInt();
            Channel ch = loadBalancer.getClientChannelById(id);
            ByteBuf buf = allocator.buffer();
            buf.writeBytes(data);
            ch.writeAndFlush(buf);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
