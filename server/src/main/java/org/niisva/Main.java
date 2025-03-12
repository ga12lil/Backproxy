package org.niisva;

import io.netty.buffer.ByteBuf;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.niisva.server.NettyServer;
import org.niisva.server.Socks5Server;

import java.util.HashMap;

@Slf4j
public class Main {
    public static void main(String[] args) throws Exception {
        int port = 8000;
        int socks5port = 1080;
        final HashMap<String, ByteBuf> targetAddresses = new HashMap<>();

        final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        final ChannelGroup socks5channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

        Thread tcpThread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new NettyServer(port, 128, channels, socks5channels).run();
                        } catch (Exception e) {
                            log.info(e.getMessage());
                        }
                    }
                }
        );

        Thread socks5Thread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new Socks5Server(socks5port, 128,  channels, socks5channels, targetAddresses).run();
                        } catch (Exception e) {
                            log.info(e.getMessage());
                        }
                    }
                }
        );
        tcpThread.start();
        socks5Thread.start();
    }
}