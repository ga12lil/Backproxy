//package org.niisva.handler;
//
//import io.netty.buffer.ByteBufAllocator;
//import io.netty.channel.Channel;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelInboundHandlerAdapter;
//import io.netty.channel.ChannelOption;
//import io.netty.channel.socket.nio.NioSocketChannel;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import io.netty.buffer.ByteBuf;
//
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//import java.util.Arrays;
//
//@Slf4j
//@RequiredArgsConstructor
//public class ClientHandler extends ChannelInboundHandlerAdapter {
//
//    private final LoadBalancer loadBalancer;
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        log.info("channelRead called");
//        int receiveBufferSize = ((NioSocketChannel) ctx.channel()).config().getOption(ChannelOption.SO_RCVBUF);
//        int sendBufferSize = ((NioSocketChannel) ctx.channel()).config().getOption(ChannelOption.SO_SNDBUF);
//
//        System.out.println("Receive buffer size: " + receiveBufferSize);
//        System.out.println("Send buffer size: " + sendBufferSize);
//        if (msg instanceof ByteBuf msgBuf) {
//            byte[] bytes = new byte[msgBuf.readableBytes()];
//            msgBuf.getBytes(msgBuf.readerIndex(), bytes);
//            byte[] idBytes = new byte[4];
//            System.arraycopy(bytes, 0,idBytes, 2, 2);
//            byte[] data = Arrays.copyOfRange(bytes, 2, bytes.length);
//            ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
//
//            int id = ByteBuffer.wrap(idBytes)
//                    .order(ByteOrder.BIG_ENDIAN)
//                    .getInt();
//            log.info("id c: {}", id);
//            Channel ch = loadBalancer.getClientChannelById(id);
//            ByteBuf buf = allocator.buffer();
//            buf.writeBytes(data);
//            ch.writeAndFlush(buf);
//        }
//    }
//
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//        cause.printStackTrace();
//        ctx.close();
//    }
//}
