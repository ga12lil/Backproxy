package org.niisva;

import lombok.extern.slf4j.Slf4j;
import org.niisva.client.WorkClient;

@Slf4j
public class Main {
    public static void main(String[] args) {
        try {
            new Node("localhost", 8000).cmdClient.connect();
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }
}