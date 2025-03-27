package org.niisva.MessageClient;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.niisva.CmdClient.CmdClient;
import org.niisva.Node;

@Slf4j
public class ProxyServerMessageHandler extends SimpleChannelInboundHandler<ByteBuf> {
    public MessageClient parentMessageClient;
    public Node parentNode;
    public ProxyServerMessageHandler(MessageClient parentCmdClient, Node parentNode)
    {
        this.parentMessageClient = parentCmdClient;
        this.parentNode = parentNode;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception
    {
        parentMessageClient.SendMessageToTargetAddressViaTargetClient(msg);
    }
}
