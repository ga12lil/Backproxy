package org.niisva;

import lombok.extern.slf4j.Slf4j;
import org.niisva.client.WorkClient;

@Slf4j
public class Main {
    public static void main(String[] args) {
        try {
            new WorkClient("localhost", 8000).connect();
        } catch (Exception e) {
            log.info(e.getMessage());
        }

    }
}