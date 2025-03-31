package org.niisva.MessageClient;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.niisva.CmdClient.CmdClient;
import org.niisva.Node;

import java.nio.ByteBuffer;
import java.util.Arrays;

@Slf4j
public class ProxyServerMessageHandler extends ChannelInboundHandlerAdapter {
    public MessageClient parentMessageClient;
    public Node parentNode;
    public int clientId;
    public ProxyServerMessageHandler(MessageClient parentCmdClient, Node parentNode, int clientId)
    {
        this.parentMessageClient = parentCmdClient;
        this.parentNode = parentNode;
        this.clientId = clientId;
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        if (msg instanceof ByteBuf buf) {
            parentMessageClient.SendMessageToTargetAddressViaTargetClient(buf);
        }

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
        ByteBuf idBuf = allocator.buffer();
        byte[] byteId = ByteBuffer.allocate(4).putInt(clientId).array();
        idBuf.writeBytes(Arrays.copyOfRange(byteId, 2, byteId.length));
        ctx.writeAndFlush(idBuf);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        parentNode.messageClients.remove(clientId);
    }
}
