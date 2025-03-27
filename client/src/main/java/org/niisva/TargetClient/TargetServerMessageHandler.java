package org.niisva.TargetClient;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.niisva.MessageClient.MessageClient;

@Slf4j
public class TargetServerMessageHandler extends SimpleChannelInboundHandler<ByteBuf> {
    public MessageClient parentMessageClient;
    public TargetClient parentTargetClient;

    public TargetServerMessageHandler(MessageClient messageClient, TargetClient targetClient)
    {
        this.parentMessageClient = messageClient;
        this.parentTargetClient = targetClient;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception
    {
        parentTargetClient.SendMessageToClientViaMessageHandler(msg);
    }
}
