package org.niisva;

import lombok.extern.slf4j.Slf4j;
import org.niisva.server.NettyServer;
import org.niisva.server.Socks5Server;
import org.niisva.util.LoadBalancer;
import org.niisva.util.SimpleClientToNodeLoadBalancer;

import java.util.HashMap;

@Slf4j
public class Main {
    public static void main(String[] args) throws Exception {
        int port = 8000;
        int socks5port = 1080;

//        final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
//        final ChannelGroup socks5channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        final LoadBalancer loadBalancer = new SimpleClientToNodeLoadBalancer(30);

        Thread tcpThread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new NettyServer(port, 128, loadBalancer).run();
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
                            new Socks5Server(socks5port, 128,  loadBalancer).run();
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