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

@Slf4j
@RequiredArgsConstructor
public class ConnectionResolver {
    private final HashMap<String, Integer> ipToClientId = new HashMap<>();
    private final HashMap<Integer, Channel> clients = new HashMap<>();
    private final HashMap<Integer, LinkedInfo> routes = new HashMap<>();
    private final Queue<Channel> nodesQueue = new LinkedList<>();

    private final int timeToLive;

    public void addClientConnection(Channel channel) {
        String clientAdr = getIpAdr(channel);
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
        return routes.get(id).getDataChannel();
    }

    public int getIdForClient(Channel channel) {
        return ipToClientId.get(getIpAdr(channel));
    }

    public Channel getClientChannel(int id) {
        return clients.get(id);
    }

    private String getIpAdr(Channel channel) {
        log.info("adr: {}",channel.remoteAddress().toString());
        return extractIp(channel.remoteAddress().toString());
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
