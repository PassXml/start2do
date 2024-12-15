package org.start2do.ebean.util;

import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Enumeration;

public class Snowflake {
    private static final int UNUSED_BITS = 1;
    private static final int EPOCH_BITS = 41;
    private static final int NODE_ID_BITS = 10;
    private static final int SEQUENCE_BITS = 12;
    private static final long maxNodeId = 1023L;
    private static final long maxSequence = 4095L;
    private static final long DEFAULT_CUSTOM_EPOCH = 1420070400000L;
    private final long nodeId;
    private final long customEpoch;
    private volatile long lastTimestamp;
    private volatile long sequence;

    public Snowflake(long nodeId, long customEpoch) {
        this.lastTimestamp = -1L;
        this.sequence = 0L;
        if (nodeId >= 0L && nodeId <= 1023L) {
            this.nodeId = nodeId;
            this.customEpoch = customEpoch;
        } else {
            throw new IllegalArgumentException(String.format("NodeId must be between %d and %d", 0, 1023L));
        }
    }

    public Snowflake(long nodeId) {
        this(nodeId, 1420070400000L);
    }

    public Snowflake() {
        this.lastTimestamp = -1L;
        this.sequence = 0L;
        this.nodeId = this.createNodeId();
        this.customEpoch = 1420070400000L;
    }

    public synchronized long nextId() {
        long currentTimestamp = this.timestamp();
        if (currentTimestamp < this.lastTimestamp) {
            throw new IllegalStateException("Invalid System Clock!");
        } else {
            if (currentTimestamp == this.lastTimestamp) {
                this.sequence = this.sequence + 1L & 4095L;
                if (this.sequence == 0L) {
                    currentTimestamp = this.waitNextMillis(currentTimestamp);
                }
            } else {
                this.sequence = 0L;
            }

            this.lastTimestamp = currentTimestamp;
            long id = currentTimestamp << 22 | this.nodeId << 12 | this.sequence;
            return id;
        }
    }

    private long timestamp() {
        return Instant.now().toEpochMilli() - this.customEpoch;
    }

    private long waitNextMillis(long currentTimestamp) {
        while(currentTimestamp == this.lastTimestamp) {
            currentTimestamp = this.timestamp();
        }

        return currentTimestamp;
    }

    private long createNodeId() {
        long nodeId;
        try {
            StringBuilder sb = new StringBuilder();
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            label29:
            while(true) {
                byte[] mac;
                do {
                    if (!networkInterfaces.hasMoreElements()) {
                        nodeId = (long)sb.toString().hashCode();
                        break label29;
                    }

                    NetworkInterface networkInterface = (NetworkInterface)networkInterfaces.nextElement();
                    mac = networkInterface.getHardwareAddress();
                } while(mac == null);

                byte[] var7 = mac;
                int var8 = mac.length;

                for(int var9 = 0; var9 < var8; ++var9) {
                    byte macPort = var7[var9];
                    sb.append(String.format("%02X", macPort));
                }
            }
        } catch (Exception var11) {
            nodeId = (long)(new SecureRandom()).nextInt();
        }

        nodeId &= 1023L;
        return nodeId;
    }

    public long[] parse(long id) {
        long maskNodeId = 4190208L;
        long maskSequence = 4095L;
        long timestamp = (id >> 22) + this.customEpoch;
        long nodeId = (id & maskNodeId) >> 12;
        long sequence = id & maskSequence;
        return new long[]{timestamp, nodeId, sequence};
    }
}
