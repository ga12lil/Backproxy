package org.niisva.util;

import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class LinkedInfo {
    private Channel linkedNode;
    private LocalDateTime ttl;
}
