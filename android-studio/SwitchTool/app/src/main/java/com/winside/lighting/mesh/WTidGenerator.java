package com.winside.lighting.mesh;

import java.util.HashMap;
import java.util.Map;

public class WTidGenerator {

    private static final Map<String, WTidGenerator> INSTANCES = new HashMap<>();

    private byte tid = 1;

    public static synchronized WTidGenerator getInstanceFor(String mac) {
        WTidGenerator generator = INSTANCES.get(mac);
        if (generator == null) {
            generator = new WTidGenerator();
            INSTANCES.put(mac, generator);
        }
        return generator;
    }

    public static synchronized void removeInstanceFor(String mac) {
        WTidGenerator generator = INSTANCES.get(mac);
        if (generator != null) {
            INSTANCES.remove(mac);
        }
    }

    public byte get() {
        return tid;
    }

    public byte getAndIncrement() {
        byte result = tid;
        tid = (byte) (tid + 1);
        return result;
    }
}
