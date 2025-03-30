package org.niisva;

import lombok.extern.slf4j.Slf4j;
import org.niisva.CmdClient.CmdClient;
import org.niisva.MessageClient.MessageClient;
import org.niisva.TargetClient.TargetClient;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class Node {
    public CmdClient cmdClient;
    public String proxyServerHost;
    public int proxyServerPort;
    public HashMap<Integer, MessageClient> messageClients;
    private final ExecutorService executorService;


    public Node(String host, int port)
    {
        this.cmdClient = new CmdClient(host, port, this);
        this.proxyServerHost = host;
        this.proxyServerPort = port;
        this.messageClients = new HashMap<Integer, MessageClient>();
        this.executorService = Executors.newCachedThreadPool();
    }

    public void NodeConnectionToProxyServer(int clientId)
    {
        if (messageClients.containsKey(clientId))
        {
            log.error("Node already connected to proxy: {}:{}", proxyServerHost, proxyServerPort);
            //TODO Отправлять какое-то сообщение прокси серверу если надо
            cmdClient.OnAlreadyConnectedToProxyServerForClient(clientId);
            return;
        }
        MessageClient mc = new MessageClient(proxyServerHost, proxyServerPort, clientId, this);
        messageClients.put(clientId, mc);
        executorService.submit(() -> {
            try
            {
                mc.connect();
            }
            catch (InterruptedException e)
            {
                log.error("Connection to proxy: {}:{} failed", proxyServerHost, proxyServerPort);
                messageClients.remove(clientId);
                cmdClient.OnFailConnectedToProxyServer(clientId);
                //TODO Отправлять какое-то сообщение прокси серверу если надо
            }
        });
    }

    public void NodeConnectionToTargetAddress(String targetHost, int targetPort, int clientId)
    {
        if (!messageClients.containsKey(clientId))
        {
            NodeConnectionToProxyServer(clientId);
        }
        MessageClient mc = messageClients.get(clientId);
        mc.ConnectionToTargetAddress(targetHost, targetPort);
    }

}
