package org.niisva.util;

import io.netty.channel.Channel;

public interface LoadBalancer {
    Channel getNodeChannelToSend(Channel channel);
    void addSocks5Connection(Channel channel);
    void addNodeConnection(Channel channel);
    Channel getClientChannelById(int id);
}
