package org.niisva;

import org.niisva.server.NettyServer;

public class Main {
    public static void main(String[] args) throws Exception {
        int port = 8000;
        new NettyServer(port, 128).run();
    }
}