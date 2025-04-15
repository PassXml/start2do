package org.start2do.script.impl.nashornl;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SandboxThreadMonitor {

    /**
     * 1毫秒 = 1,000,000纳秒
     */
    private static final int MILLI_TO_NANO = 1000000;

    /**
     * 单位：纳秒，允许线程使用CPU的最大时间
     */
    private final long maxCPUTime;

    /**
     * 单位：byte，允许线程分配的最大内存
     */
    private final long maxMemory;

    /**
     * 单位：毫秒，当前使用的CPU时间
     */
    private long cpuRuntime;

    /**
     * 单位：byte，当前分配的内存大小
     */
    private long allocatedMemory;

    private boolean cpuTimeExceeded;

    private boolean memoryExceeded;

    /**
     * 监控的线程
     */
    private Thread threadToMonitor;

    /**
     * 要监控的线程是否已经初始化完成
     */
    private CountDownLatch threadToMonitorCountDownLatch;

    /**
     * 用于测量线程CPU时间
     */
    private ThreadMXBean threadMXBean;

    private com.sun.management.ThreadMXBean memoryThreadMXBean;

    private boolean stop;

    public SandboxThreadMonitor(final long maxCPUTime, final long maxMemory) {
        this.maxCPUTime = maxCPUTime * MILLI_TO_NANO;
        this.maxMemory = maxMemory;
        this.threadToMonitorCountDownLatch = new CountDownLatch(1);

        if (maxCPUTime > 0) {
            threadMXBean = ManagementFactory.getThreadMXBean();
            // 保证可以测量CPU时间
            threadMXBean.setThreadCpuTimeEnabled(true);
        }

        if (Objects.nonNull(threadMXBean) && threadMXBean instanceof com.sun.management.ThreadMXBean) {
            if (maxMemory > 0) {
                memoryThreadMXBean = (com.sun.management.ThreadMXBean) threadMXBean;
                // 保证可以测量内存使用量
                memoryThreadMXBean.setThreadAllocatedMemoryEnabled(true);
            }
        } else if (maxMemory > 0) {
            throw new UnsupportedOperationException("Thread allocated memory measurement is not supported.");
        }
    }

    public void setThreadToMonitor(Thread threadToMonitor) {
        this.threadToMonitor = threadToMonitor;
        this.threadToMonitorCountDownLatch.countDown();
    }

    private void forceStopThread() {
        try {
            // 尝试中断
            threadToMonitor.interrupt();

            // 给予一个短暂的时间让线程响应中断
            if (threadToMonitor.isAlive()) {
                Thread.sleep(500);
            }

            // 如果线程仍然存活，则使用 stop 方法强制终止
            // 注意：这是一个不安全的操作，但在沙箱环境中可以接受
            if (threadToMonitor.isAlive()) {
                threadToMonitor.stop();
            }
        } catch (Exception e) {
            // 忽略异常
        }
    }

    /**
     * 主线程运行监控器。阻塞主线程，直至脚本结束
     * <p>并不是{@link Runnable#run()}方法
     */
    public void run() throws InterruptedException {
        this.threadToMonitorCountDownLatch.await();
        final long startCPUTime = getCPUTime();
        final long startMemory = getMemoryAllocated();
        while (!stop) {
            final long runtimeNanosecond = getCPUTime() - startCPUTime;
            final long memoryByte = getMemoryAllocated() - startMemory;
            boolean cpuTimeExceeded = isCPUTimeExceeded(runtimeNanosecond);
            boolean memoryExceeded = isMemoryExceeded(memoryByte);
            if (cpuTimeExceeded || memoryExceeded) {
                this.cpuTimeExceeded = cpuTimeExceeded;
                this.memoryExceeded = memoryExceeded;
                this.cpuRuntime = runtimeNanosecond / MILLI_TO_NANO;
                this.allocatedMemory = memoryByte;
                // 中断脚本线程
                forceStopThread();
            }
            TimeUnit.MILLISECONDS.sleep(100);
        }
    }

    private boolean isMemoryExceeded(long memoryByte) {
        if (maxMemory <= 0) {
            return false;
        }
        return memoryByte > maxMemory;
    }

    private boolean isCPUTimeExceeded(long runtimeNanosecond) {
        if (maxCPUTime <= 0) {
            return false;
        }
        return runtimeNanosecond > maxCPUTime;
    }

    /**
     * 获取当前监控线程分配了的内存
     *
     * @return 当前监控线程分配了的内存，单位bytes
     */
    private long getMemoryAllocated() {
        if (maxMemory > 0 && Objects.nonNull(memoryThreadMXBean)) {
            return memoryThreadMXBean.getThreadAllocatedBytes(threadToMonitor.getId());
        }
        return 0;
    }

    /**
     * 获取当前监控线程CPU时间
     *
     * @return 当前线程CPU时间，单位nanoseconds
     */
    private long getCPUTime() {
        if (maxCPUTime > 0 && Objects.nonNull(threadMXBean)) {
            return threadMXBean.getThreadCpuTime(threadToMonitor.getId());
        }
        return 0;
    }

    /**
     * 停止监控
     */
    public void stop() {
        this.stop = true;
    }

    public long getCpuRuntime() {
        return cpuRuntime;
    }

    public long getAllocatedMemory() {
        return allocatedMemory;
    }

    public boolean isCpuTimeExceeded() {
        return cpuTimeExceeded;
    }

    public boolean isMemoryExceeded() {
        return memoryExceeded;
    }
}
