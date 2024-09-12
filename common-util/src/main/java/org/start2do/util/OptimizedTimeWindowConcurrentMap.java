package org.start2do.util;


import java.util.concurrent.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.*;

public class OptimizedTimeWindowConcurrentMap<K, V> {

    private final long windowSizeInMillis;
    private final int numWindows;
    private final List<List<ConcurrentHashMap<K, V>>> windows;
    private final ScheduledExecutorService scheduler;
    private final ConcurrentLinkedQueue<ConcurrentHashMap<K, V>> mapPool;
    private final int shardCount;  // 数据分片数
    private final List<ExpiryListener<K, V>> listeners;  // 存储所有订阅者
    private final ReadWriteLock[] windowLocks;  // 每个窗口一个读写锁

    private final int initialPoolSize;  // 对象池初始大小
    private final int minPoolSize;  // 对象池最小大小
    private final int maxPoolSize;  // 对象池最大大小
    private final AtomicInteger currentPoolSize;  // 当前对象池大小
    private final ScheduledExecutorService poolAdjuster;  // 对象池调整器
    private final ForkJoinPool forkJoinPool = new ForkJoinPool(2);

    // 订阅者接口
    public interface ExpiryListener<K, V> {

        void onExpiry(List<ConcurrentHashMap<K, V>> expiredData);
    }

    /**
     * 构造函数; 过期时间: 窗口大小*数量
     *
     * @param windowSizeInMillis 窗口大小，单位毫秒
     * @param numWindows         窗口数量
     * @param shardCount         数据分片数
     * @param initialPoolSize    对象池初始大小
     * @param minPoolSize        对象池最小大小
     * @param maxPoolSize        对象池最大大小
     */
    public OptimizedTimeWindowConcurrentMap(long windowSizeInMillis, int numWindows, int shardCount,
        int initialPoolSize, int minPoolSize, int maxPoolSize) {
        this.windowSizeInMillis = windowSizeInMillis;
        this.numWindows = numWindows;
        this.shardCount = shardCount;
        this.initialPoolSize = initialPoolSize;
        this.minPoolSize = minPoolSize;
        this.maxPoolSize = maxPoolSize;
        this.currentPoolSize = new AtomicInteger(initialPoolSize);
        this.windows = new ArrayList<>(numWindows);
        this.listeners = new ArrayList<>(); // 初始化订阅者列表
        this.windowLocks = new ReentrantReadWriteLock[numWindows]; // 初始化读写锁数组

        // 初始化对象池
        mapPool = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < initialPoolSize; i++) {
            mapPool.offer(new ConcurrentHashMap<>());
        }

        // 初始化窗口列表，并从对象池获取 ConcurrentHashMap
        for (int i = 0; i < numWindows; i++) {
            List<ConcurrentHashMap<K, V>> shards = new ArrayList<>(shardCount);
            for (int j = 0; j < shardCount; j++) {
                shards.add(createNewShardMap());
            }
            windows.add(shards);
            windowLocks[i] = new ReentrantReadWriteLock(); // 初始化读写锁
        }

        // 定期清理过期的窗口
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::rotateWindows, windowSizeInMillis, windowSizeInMillis,
            TimeUnit.MILLISECONDS);

        // 定期调整对象池大小
        poolAdjuster = Executors.newScheduledThreadPool(1);
        poolAdjuster.scheduleAtFixedRate(this::adjustPoolSize, 10, 10, TimeUnit.SECONDS); // 每10秒调整一次对象池大小
    }

    // 使用对象池创建新的ShardMap
    private ConcurrentHashMap<K, V> createNewShardMap() {
        ConcurrentHashMap<K, V> map = mapPool.poll();
        if (map == null && currentPoolSize.get() < maxPoolSize) {
            currentPoolSize.incrementAndGet(); // 增加池大小
            return new ConcurrentHashMap<>();
        }
        return map != null ? map : new ConcurrentHashMap<>(); // 如果超出最大池大小，仍然返回一个新的对象
    }

    // 将ConcurrentHashMap归还给对象池
    private void returnShardMap(ConcurrentHashMap<K, V> map) {
        map.clear();
        if (currentPoolSize.get() > maxPoolSize || mapPool.size() >= maxPoolSize) {
            currentPoolSize.decrementAndGet(); // 减少池大小
        } else {
            mapPool.offer(map);
        }
    }

    // 将key-value存入当前窗口的分片中
    public void put(K key, V value) {
        int shardIndex = getShardIndex(key);
        List<ConcurrentHashMap<K, V>> currentWindow = getCurrentWindow();
        ReadWriteLock lock = windowLocks[getCurrentWindowIndex()];
        lock.readLock().lock(); // 获取读锁
        try {
            currentWindow.get(shardIndex).put(key, value);
        } finally {
            lock.readLock().unlock(); // 释放读锁
        }
    }

    // 从所有窗口的所有分片中获取key对应的value
    public V get(K key) {
        int shardIndex = getShardIndex(key);
        for (int i = 0; i < numWindows; i++) {
            ReadWriteLock lock = windowLocks[i];
            lock.readLock().lock(); // 获取读锁
            try {
                V value = windows.get(i).get(shardIndex).get(key);
                if (value != null) {
                    return value;
                }
            } finally {
                lock.readLock().unlock(); // 释放读锁
            }
        }
        return null;
    }

    // 获取当前活动窗口的所有分片
    private List<ConcurrentHashMap<K, V>> getCurrentWindow() {
        return windows.get(getCurrentWindowIndex());
    }

    // 获取当前窗口索引
    private int getCurrentWindowIndex() {
        long currentTime = System.currentTimeMillis();
        return (int) ((currentTime / windowSizeInMillis) % numWindows);
    }

    // 自定义hash策略来获取shard的索引
    private int getShardIndex(K key) {
        return Math.abs(key.hashCode()) % shardCount;
    }

    // 定期清理过期窗口
    private void rotateWindows() {
        long currentTime = System.currentTimeMillis();
        int expiredIndex = (int) (((currentTime - windowSizeInMillis * numWindows) / windowSizeInMillis) % numWindows);
        ReadWriteLock lock = windowLocks[expiredIndex];

        lock.writeLock().lock(); // 获取写锁
        try {
            List<ConcurrentHashMap<K, V>> expiredWindow = windows.get(expiredIndex);
            ForkJoinTask<?> task = forkJoinPool.submit(() -> {
                // 通知所有订阅者
                notifyListeners(expiredWindow);
                // 清理过期窗口
                for (ConcurrentHashMap<K, V> shard : expiredWindow) {
                    returnShardMap(shard);
                }
            });
            ForkJoinTask<?> task1 = forkJoinPool.submit(() -> {
                // 创建新的窗口
                List<ConcurrentHashMap<K, V>> newWindow = new ArrayList<>(shardCount);
                for (int j = 0; j < shardCount; j++) {
                    newWindow.add(createNewShardMap());
                }
                windows.set(expiredIndex, newWindow);
            });
            //等待task1和task完成
            task.join();
            task1.join();

        } finally {
            lock.writeLock().unlock(); // 释放写锁
        }
    }

    // 通知订阅者
    private void notifyListeners(List<ConcurrentHashMap<K, V>> expiredData) {
        for (ExpiryListener<K, V> listener : listeners) {
            listener.onExpiry(expiredData);
        }
    }

    // 添加订阅者
    public void addExpiryListener(ExpiryListener<K, V> listener) {
        listeners.add(listener);
    }

    // 移除订阅者
    public void removeExpiryListener(ExpiryListener<K, V> listener) {
        listeners.remove(listener);
    }

    // 调整对象池大小
    private void adjustPoolSize() {
        int size = mapPool.size();
        if (size < minPoolSize) {
            for (int i = 0; i < (minPoolSize - size); i++) {
                mapPool.offer(new ConcurrentHashMap<>());
                currentPoolSize.incrementAndGet();
            }
        } else if (size > maxPoolSize) {
            for (int i = 0; i < (size - maxPoolSize); i++) {
                mapPool.poll();
                currentPoolSize.decrementAndGet();
            }
        }
    }

    // 关闭调度器
    public void shutdown() {
        scheduler.shutdown();
        poolAdjuster.shutdown();
        mapPool.clear();
        listeners.clear();
    }

    public static void main(String[] args) throws InterruptedException {
        OptimizedTimeWindowConcurrentMap<String, String> map = new OptimizedTimeWindowConcurrentMap<>(1000, 5, 16, 10,
            5, 20); // 窗口大小为5秒，共5个窗口，16个分片

        // 添加订阅者
        map.addExpiryListener(expiredData -> {
            System.out.println(DateUtil.LocalDateTimeStr() + "Expired data: " + expiredData);
        });

//        // 并发测试
//        Runnable task = () -> {
//            for (int i = 0; i < 1000; i++) {
//                map.put("key" + i, DateUtil.LocalDateTimeStr());
//                try {
//                    Thread.sleep(200);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//
//            }
//        };
//        Thread t1 = new Thread(task);
//        Thread t2 = new Thread(task);
//        t1.start();
//        t2.start();
//        t1.join();
//        t2.join();
//
//        // 不同时间窗口的测试
//        for (int i = 0; i < 10; i++) {
//            map.put("key" + i, DateUtil.LocalDateTimeStr());
//            Thread.sleep(200);
//        }
//
//
//        System.out.println(map.get("key1")); // 可能输出null，因为数据已经过期
        map.put("key1", DateUtil.LocalDateTimeStr());
        Thread.sleep(4000); // 等待6秒
        System.out.println(DateUtil.LocalDateTimeStr() + "GetValue" + map.get("key1")); // 可能输出null，因为数据已经过期
        Thread.sleep(4000); // 等待6秒
        System.out.println(DateUtil.LocalDateTimeStr() + "GetValue" + map.get("key1")); // 可能输出null，因为数据已经过期

        new Scanner(System.in).next();
        map.shutdown();
    }
}
