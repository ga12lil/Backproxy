package org.niisva.TargetClient;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.niisva.MessageClient.MessageClient;

@Slf4j
public class TargetServerMessageHandler extends ChannelInboundHandlerAdapter {
    public MessageClient parentMessageClient;
    public TargetClient parentTargetClient;

    public TargetServerMessageHandler(MessageClient messageClient, TargetClient targetClient)
    {
        this.parentMessageClient = messageClient;
        this.parentTargetClient = targetClient;
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        if (msg instanceof ByteBuf buf) {
            parentTargetClient.SendMessageToClientViaMessageHandler(buf);

            log.info("done");
        }

    }
}
