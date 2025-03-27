package org.niisva.CmdClient;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.niisva.Node;

@Slf4j
public class ProxyServerCmdHandler extends SimpleChannelInboundHandler<ByteBuf>{
    public CmdClient parentCmdClient;
    public Node parentNode;
    public ProxyServerCmdHandler(CmdClient parentCmdClient, Node parentNode)
    {
        this.parentCmdClient = parentCmdClient;
        this.parentNode = parentNode;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception
    {
        parentCmdClient.ReadStartCommand(msg);
    }
}
