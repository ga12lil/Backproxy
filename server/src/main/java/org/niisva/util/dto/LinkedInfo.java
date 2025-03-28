package org.niisva.util.dto;

import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class LinkedInfo {
    private Channel serviceChannel;
    private Channel dataChannel;
    private LocalDateTime ttl;
}
