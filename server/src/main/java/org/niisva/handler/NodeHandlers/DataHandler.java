package org.niisva.handler.NodeHandlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import org.niisva.handler.Socks5ServerHandler;
import org.niisva.util.ConnectionResolver;

@RequiredArgsConstructor
public class DataHandler extends ChannelInboundHandlerAdapter {
    private int id = -1;
    private final ConnectionResolver connectionResolver;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf msgBuf) {
            if (id == -1) {
                id = msgBuf.getUnsignedShort(0);
                connectionResolver.addNodeDataConnection(ctx.channel(),id);
                Socks5ServerHandler handler = connectionResolver.getClientChannel(id)
                        .pipeline()
                        .get(Socks5ServerHandler.class);

                if (handler != null) {
                    handler.setFutureComplete(null);
                }

            }
            else {
                Channel client = connectionResolver.getClientChannel(id);
                if (client != null) {
                    client.writeAndFlush(msgBuf);
                }
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        connectionResolver.disconnectDataChannel(ctx.channel());
    }
}
