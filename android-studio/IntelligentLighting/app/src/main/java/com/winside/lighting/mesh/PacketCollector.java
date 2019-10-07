package com.winside.lighting.mesh;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class PacketCollector {

    private final PacketFilter packetFilter;

    private final LinkedBlockingQueue<Packet> resultQueue;

    public PacketCollector(PacketFilter packetFilter) {
        super();
        this.packetFilter = packetFilter;
        this.resultQueue = new LinkedBlockingQueue<>();
    }

    @SuppressWarnings("unchecked")
    public final <P extends Packet> P nextResultBlock() throws InterruptedException {
        P res = null;
        while (res == null) {
            res = (P) resultQueue.take();
        }
        return res;
    }

    public final <P extends Packet> P nextResult() throws InterruptedException {
        return nextResult(10 * 1000);
    }

    private volatile long waitStart;

    @SuppressWarnings("unchecked")
    public final <P extends Packet> P nextResult(long timeout) throws InterruptedException {
        P res;
        long remainingWait = timeout;
        waitStart = System.currentTimeMillis();
        do {
            res = (P) resultQueue.poll(remainingWait, TimeUnit.MILLISECONDS);
            if (res != null) {
                return res;
            }
            remainingWait = timeout - (System.currentTimeMillis() - waitStart);
        } while (remainingWait > 0);
        return null;
    }

    public final void processPacket(Packet packet) {
        if (packetFilter == null || packetFilter.accept(packet)) {
            while (!resultQueue.offer(packet)) {
                resultQueue.poll();
            }
        }
    }
}
