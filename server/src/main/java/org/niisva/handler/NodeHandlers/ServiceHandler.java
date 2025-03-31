package org.niisva.handler.NodeHandlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.niisva.handler.Socks5ServerHandler;
import org.niisva.util.ConnectionResolver;

@Slf4j
@RequiredArgsConstructor
public class ServiceHandler extends ChannelInboundHandlerAdapter {

    private final ConnectionResolver connectionResolver;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //todo обработку ошибок команд, в первую очередь обработку неудачного соединения с целевым хостом
        if (msg instanceof ByteBuf msgBuf) {
            int type = msgBuf.getByte(0);
            if (type == 2) {
                int id = msgBuf.getUnsignedShort(0);
                ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
                ByteBuf answer = allocator.buffer();
                answer.writeInt(0);
                connectionResolver.getClientChannel(id).writeAndFlush(answer);
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        connectionResolver.disconnectServiceChannel(ctx.channel());
    }
}
