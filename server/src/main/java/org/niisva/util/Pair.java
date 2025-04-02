package org.niisva.util;
import java.util.Objects;

public class Pair {
    public String host;
    public int port;
    public Pair(String host, int port)
    {
        this.host = host;
        this.port = port;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Pair pair = (Pair) obj;
        return port == pair.port && Objects.equals(host, pair.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }
}
