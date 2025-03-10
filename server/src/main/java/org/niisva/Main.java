package org.niisva;

import lombok.extern.slf4j.Slf4j;
import org.niisva.server.NettyServer;
import org.niisva.server.Socks5Server;

@Slf4j
public class Main {
    public static void main(String[] args) throws Exception {
        int port = 8000;
        int socks5port = 1080;

        Thread tcpThread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new NettyServer(port, 128).run();
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
                            new Socks5Server(socks5port, 128).run();
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