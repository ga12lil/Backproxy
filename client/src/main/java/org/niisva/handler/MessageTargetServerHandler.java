package org.niisva.handler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.niisva.TargetRequest;
import org.niisva.client.WorkClient;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;

@Slf4j
public class MessageTargetServerHandler extends SimpleChannelInboundHandler<ByteBuf> {

    public WorkClient parentClient;
    public TargetRequest parentRequest;

    public MessageTargetServerHandler(WorkClient client, TargetRequest parentRequest) {
        this.parentClient = client;
        this.parentRequest = parentRequest;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {

        log.info("channelRead0 was called");

        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes);
        log.info("Answer: {}", new String(bytes, StandardCharsets.UTF_8));
        parentClient.channel.writeAndFlush(Unpooled.wrappedBuffer(bytes));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        parentClient.requests.remove(parentRequest);
        log.info("Count: {}", parentClient.requests.size());
        log.info("Connection closed with address: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Exception in MessageHandler", cause);
        ctx.close();
    }
}
