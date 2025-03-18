package org.niisva.util;

import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

@RequiredArgsConstructor
public class SimpleClientToNodeLoadBalancer implements LoadBalancer{

    private final HashMap<String, LinkedInfo> routes = new HashMap<>();
    private final Queue<Channel> nodesQueue = new LinkedList<>();
    private final HashMap<Integer, Channel> clients = new HashMap<>();
    private final int timeToLive;
    @Override
    public Channel getNodeChannelToSend (Channel channel) {
        String clientAdr = channel.remoteAddress().toString().split("]")[0].split("\\[")[1];
        LinkedInfo route = routes.get(clientAdr);
        if (route != null) {
            long secondsPassed = ChronoUnit.SECONDS.between(route.getTtl(), LocalDateTime.now());
            if (secondsPassed > timeToLive) {
                route.setLinkedNode(getNextChannelFromQueue());
            }
            route.setTtl(LocalDateTime.now());

        }
        else {
            route = new LinkedInfo(getNextChannelFromQueue(), LocalDateTime.now());
            routes.put(clientAdr, route);
        }
        return route.getLinkedNode();
    }

    @Override
    public void addSocks5Connection(Channel channel) {
        Random random = new Random();
        int randomId = random.nextInt(65536);
        while (clients.get(randomId) != null) {
            randomId = random.nextInt(65536);
        }
        clients.put(randomId, channel);
    }

    @Override
    public void addNodeConnection(Channel channel) {
        if (nodesQueue.stream().noneMatch(node -> node==channel)) {
            nodesQueue.add(channel);
        }
    }

    @Override
    public Channel getClientChannelById(int id) {
        Channel channel = clients.get(id);
        if(channel!=null) {
            if (!channel.isActive()){
                clients.remove(id);
            }
        }
        return channel;
    }

    private Channel getNextChannelFromQueue() {
        while (!nodesQueue.isEmpty()) {
            Channel ch = nodesQueue.poll();
            if (ch.isActive()) {
                nodesQueue.add(ch);
                return ch;
            }
        }
        return null;
    }


}
