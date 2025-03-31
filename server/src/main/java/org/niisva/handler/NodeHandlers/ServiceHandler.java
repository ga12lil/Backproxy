package org.niisva.handler.NodeHandlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.niisva.util.ConnectionResolver;

@Slf4j
@RequiredArgsConstructor
public class ServiceHandler extends ChannelInboundHandlerAdapter {

    private final ConnectionResolver connectionResolver;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //todo обработку ошибок команд, в первую очередь обработку неудачного соединения с целевым хостом
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        connectionResolver.disconnectServiceChannel(ctx.channel());
    }
}
