package org.niisva.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.niisva.TargetRequest;
import org.niisva.client.WorkClient;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class MessageProxyServerHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private WorkClient parentClient;
    private final ExecutorService executorService;

    public MessageProxyServerHandler(WorkClient client) {
        this.parentClient = client;
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        log.info("channelRead0 waw called");

        int hostLength = msg.readByte();
        byte[] hostBytes = new byte[hostLength];
        msg.readBytes(hostBytes);
        String host = new String(hostBytes, StandardCharsets.UTF_8);

        int port = msg.readUnsignedShort();

        byte[] remainingData = new byte[msg.readableBytes()];
        msg.readBytes(remainingData);
        ByteBuf data = Unpooled.wrappedBuffer(remainingData);

        executorService.submit(() -> {
            int index = -1;

            for (int i = 0; i < parentClient.requests.size(); i++) {
                if (parentClient.requests.get(i).port == port && parentClient.requests.get(i).host.equals(host)) {
                    index = i;
                    break;
                }
            }

            if (index != -1) {
                log.info("Send to host: {}, port: {}", host, port);
                parentClient.requests.get(index).bytes = data;
                parentClient.requests.get(index).SendingRequest();
            } else {
                log.info("Connected to host: {}, port: {}", host, port);
                parentClient.requests.add(new TargetRequest(host, port, data, parentClient));
                try {
                    parentClient.requests.get(parentClient.requests.size() - 1).connect();
                } catch (InterruptedException e) {
                    log.error("Error connecting to target server", e);
                }
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Exception in MessageHandler", cause);
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        executorService.shutdown();
        log.info("Connection closed with address: {}", ctx.channel().remoteAddress());
    }
}
