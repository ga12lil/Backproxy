package org.niisva.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.niisva.util.dto.LinkedInfo;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.niisva.util.Pair;
import java.net.InetSocketAddress;

@Slf4j
@RequiredArgsConstructor
public class ConnectionResolver {
    //private final ConcurrentHashMap<String, Integer> ipToClientId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Pair, Integer> ipToClientId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Channel> clients = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, LinkedInfo> routes = new ConcurrentHashMap<>();
    private final Queue<Channel> nodesQueue = new LinkedList<>();

    private final int timeToLive;

    public void addClientConnection(Channel channel) {
        //String clientAdr = getIpAdr(channel);
        Pair clientAdr = getAdr(channel);
        Integer id = ipToClientId.get(clientAdr);
        if (id == null) {
            Random random = new Random();
            int randomId = random.nextInt(65536);
            while (clients.get(randomId) != null) {
                randomId = random.nextInt(65536);
            }
            clients.put(randomId, channel);
            ipToClientId.put(clientAdr, randomId);
            id = randomId;
        }
        else {
            clients.put(id, channel);
        }

        LinkedInfo route = routes.get(id);

        if (route != null) {
            long secondsPassed = ChronoUnit.SECONDS.between(route.getTtl(), LocalDateTime.now());
            if (secondsPassed > timeToLive) {
                route.setServiceChannel(getNextChannelFromQueue());
                route.setDataChannel(null);
            }
            route.setTtl(LocalDateTime.now());

        }
        else {
            route = new LinkedInfo(getNextChannelFromQueue(), null, LocalDateTime.now());
            routes.put(id, route);
        }

        Channel dataChannel = route.getDataChannel();
        if (dataChannel==null || !dataChannel.isActive()) {
            route.setDataChannel(null);
            byte[] byteId = ByteBuffer.allocate(4).putInt(id).array();
            ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
            ByteBuf idBuf = allocator.buffer();
            idBuf.writeBytes(Arrays.copyOfRange(byteId, 2, byteId.length));

            byte msgType = (byte) 1;
            ByteBuf msgTypeBuf = allocator.buffer();
            msgTypeBuf.writeByte(msgType);
            route.getServiceChannel().write(msgTypeBuf);
            route.getServiceChannel().writeAndFlush(idBuf);
        }
    }

    public Channel tryGetServiceConnection(Channel channel) {
        String nodeAdr = getIpAdr(channel);
        Optional<Channel> sameIpChannel = nodesQueue.stream()
                .filter(node -> getIpAdr(node).equals(nodeAdr))
                .findFirst();
        return sameIpChannel.orElse(null);
    }

    public void addNodeServiceConnection(Channel channel) {
        nodesQueue.add(channel);
    }

    public void addNodeDataConnection(Channel channel, int id) {
        LinkedInfo linkedInfo = routes.get(id);
        if (linkedInfo != null) {
            linkedInfo.setDataChannel(channel);
        }
    }

    public Channel getServiceChannel(Channel channel) {
        int id = getIdForClient(channel);
        return routes.get(id).getServiceChannel();
    }

    public Channel getDataChannel(Channel channel) {
        int id = getIdForClient(channel);
        LinkedInfo route = routes.get(id);
        Channel dataChannel = route.getDataChannel();
        if (dataChannel!=null && !dataChannel.isActive()) {
            routes.remove(id);
            addClientConnection(channel);
            return null;
        }
        return dataChannel;
    }

    public int getIdForClient(Channel channel) {
        //String str = getIpAdr(channel);
        Pair pair = getAdr(channel);
        /*
        log.info("STR: {}:{} cnt: {}", pair.host, pair.port, ipToClientId.values().size());
        for (Pair p: ipToClientId.keySet())
        {
            log.info("in HasmMap keys: {}:{}", p.host, p.port);
        }
        for (int p: ipToClientId.values())
        {
            log.info("in HasmMap values: {}:{}", p);
        }
        */
        //int id = ipToClientId.get(str);
        int id = ipToClientId.get(pair);
        return id;
        //Pair pair = getAdr(channel);
        //log.info("host: {} port: {}", pair.host, pair.port);
        //return ipToClientId.get(pair);
    }

    public Channel getClientChannel(int id) {
        return clients.get(id);
    }

    public void disconnectClient (Channel channel) {
        //disconnectDataChannel(routes.get(getIdForClient(channel)).getDataChannel());

    }

    public void disconnectDataChannel (Channel channel) {
        if(channel.isActive()) {
            channel.close();
        }
        routes.entrySet().stream()
                .filter(route -> route.getValue().getDataChannel()==channel)
                .forEach(route -> {
                    route.getValue().setDataChannel(null);
                });
    }

    public void disconnectServiceChannel (Channel channel) {
        nodesQueue.remove(channel);
        routes.entrySet().stream()
                .filter(route -> route.getValue().getServiceChannel()==channel)
                .forEach(route -> {
                    routes.remove(route.getKey());
                });
    }

    public void cleanDisconnected() {
        routes.entrySet().stream().filter(route -> {
            long secondsPassed = ChronoUnit.SECONDS.between(route.getValue().getTtl(), LocalDateTime.now());
            if (secondsPassed > timeToLive) {
                if (!clients.get(route.getKey()).isActive()) {
                    return true;
                }
                else {
                    route.getValue().setTtl(LocalDateTime.now());
                }
            }
            return false;
        }).forEach(route -> {
            int id = route.getKey();
            routes.remove(id);
            ipToClientId.remove(getIpAdr(clients.get(id)));
            clients.remove(id);
        });
    }

    private String getIpAdr(Channel channel) {
        log.info("adr: {}",channel.remoteAddress().toString());
        return extractIp(channel.remoteAddress().toString());
    }

    private Pair getAdr(Channel channel) {
        InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();
        String remoteHost = remoteAddress.getHostString();
        int remotePort = remoteAddress.getPort();
        return new Pair(remoteHost, remotePort);
    }

    private String extractIp(String input) {
        String withoutSlash = input.substring(1);
        if (withoutSlash.startsWith("[")) {
            return withoutSlash.substring(1, withoutSlash.indexOf(']'));
        } else {
            return withoutSlash.substring(0, withoutSlash.indexOf(':'));
        }
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
