package com.allen.thumb.manager.cache;

import cn.hutool.core.util.HashUtil;
import lombok.Data;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * HeavyKeeper类实现了TopK接口，用于维护一个近似的TopK元素集合。
 * 它通过使用计数最小堆和哈希桶的数据结构来实现。
 */
public class HeavyKeeper implements TopK {
    // 查找表大小
    private static final int LOOKUP_TABLE_SIZE = 256;
    // TopK的K值
    private final int k;
    // 宽度，即每个深度层级的桶数量
    private final int width;
    // 深度，即桶的层数
    private final int depth;
    // 查找表，用于存储衰减因子
    private final double[] lookupTable;
    // 桶数组，用于存储元素指纹和计数
    private final Bucket[][] buckets;
    // 最小堆，用于维护TopK元素
    private final PriorityQueue<Node> minHeap;
    // 被驱逐元素队列
    private final BlockingQueue<Item> expelledQueue;
    // 随机数生成器
    private final Random random;
    // 总计数
    private long total;
    // 最小计数阈值
    private final int minCount;

    /**
     * 构造函数，初始化HeavyKeeper实例
     * @param k TopK的K值
     * @param width 宽度，即每个深度层级的桶数量
     * @param depth 深度，即桶的层数
     * @param decay 衰减因子
     * @param minCount 最小计数阈值
     */
    public HeavyKeeper(int k, int width, int depth, double decay, int minCount) {
        this.k = k;
        this.width = width;
        this.depth = depth;
        this.minCount = minCount;

        this.lookupTable = new double[LOOKUP_TABLE_SIZE];
        for (int i = 0; i < LOOKUP_TABLE_SIZE; i++) {
            lookupTable[i] = Math.pow(decay, i);
        }

        this.buckets = new Bucket[depth][width];
        for (int i = 0; i < depth; i++) {
            for (int j = 0; j < width; j++) {
                buckets[i][j] = new Bucket();
            }
        }

        this.minHeap = new PriorityQueue<>(Comparator.comparingInt(n -> n.count));
        this.expelledQueue = new LinkedBlockingQueue<>();
        this.random = new Random();
        this.total = 0;
    }

    /**
     * 添加元素到HeavyKeeper中
     * @param key 元素键
     * @param increment 元素计数增量
     * @return 添加结果，包括是否进入TopK、被挤出的元素等信息
     */
    @Override
    public AddResult add(String key, int increment) {
        byte[] keyBytes = key.getBytes();
        long itemFingerprint = hash(keyBytes);
        int maxCount = 0;

        for (int i = 0; i < depth; i++) {
            int bucketNumber = Math.abs(hash(keyBytes)) % width;
            Bucket bucket = buckets[i][bucketNumber];

            synchronized (bucket) {
                if (bucket.count == 0) {
                    bucket.fingerprint = itemFingerprint;
                    bucket.count = increment;
                    maxCount = Math.max(maxCount, increment);
                } else if (bucket.fingerprint == itemFingerprint) {
                    bucket.count += increment;
                    maxCount = Math.max(maxCount, bucket.count);
                } else {
                    for (int j = 0; j < increment; j++) {
                        double decay = bucket.count < LOOKUP_TABLE_SIZE ?
                                lookupTable[bucket.count] :
                                lookupTable[LOOKUP_TABLE_SIZE - 1];
                        if (random.nextDouble() < decay) {
                            bucket.count--;
                            if (bucket.count == 0) {
                                bucket.fingerprint = itemFingerprint;
                                bucket.count = increment - j;
                                maxCount = Math.max(maxCount, bucket.count);
                                break;
                            }
                        }
                    }
                }
            }
        }

        total += increment;

        if (maxCount < minCount) {
            return new AddResult(null, false, null);
        }

        synchronized (minHeap) {
            boolean isHot = false;
            String expelled = null;

            Optional<Node> existing = minHeap.stream()
                    .filter(n -> n.key.equals(key))
                    .findFirst();

            if (existing.isPresent()) {
                minHeap.remove(existing.get());
                minHeap.add(new Node(key, maxCount));
                isHot = true;
            } else {
                if (minHeap.size() < k || maxCount >= Objects.requireNonNull(minHeap.peek()).count) {
                    Node newNode = new Node(key, maxCount);
                    if (minHeap.size() >= k) {
                        expelled = minHeap.poll().key;
                        expelledQueue.offer(new Item(expelled, maxCount));
                    }
                    minHeap.add(newNode);
                    isHot = true;
                }
            }

            return new AddResult(expelled, isHot, key);
        }
    }

    /**
     * 获取当前TopK元素列表
     * @return TopK元素列表
     */
    @Override
    public List<Item> list() {
        synchronized (minHeap) {
            List<Item> result = new ArrayList<>(minHeap.size());
            for (Node node : minHeap) {
                result.add(new Item(node.key, node.count));
            }
            result.sort((a, b) -> Integer.compare(b.count(), a.count()));
            return result;
        }
    }

    /**
     * 获取被驱逐元素队列
     * @return 被驱逐元素队列
     */
    @Override
    public BlockingQueue<Item> expelled() {
        return expelledQueue;
    }

    /**
     * 执行计数衰减操作，将所有元素的计数减半
     */
    @Override
    public void fading() {
        for (Bucket[] row : buckets) {
            for (Bucket bucket : row) {
                synchronized (bucket) {
                    bucket.count = bucket.count >> 1;
                }
            }
        }

        synchronized (minHeap) {
            PriorityQueue<Node> newHeap = new PriorityQueue<>(Comparator.comparingInt(n -> n.count));
            for (Node node : minHeap) {
                newHeap.add(new Node(node.key, node.count >> 1));
            }
            minHeap.clear();
            minHeap.addAll(newHeap);
        }

        total = total >> 1;
    }

    /**
     * 获取总计数
     * @return 总计数
     */
    @Override
    public long total() {
        return total;
    }

    // 桶类，用于存储元素指纹和计数
    private static class Bucket {
        long fingerprint;
        int count;
    }

    // 节点类，用于最小堆中存储元素键和计数
    private static class Node {
        final String key;
        final int count;

        Node(String key, int count) {
            this.key = key;
            this.count = count;
        }
    }

    // 哈希函数，生成元素的哈希值
    private static int hash(byte[] data) {
        return HashUtil.murmur32(data);
    }

}

// 新增返回结果类
@Data
class AddResult {
    // 被挤出的 key
    private final String expelledKey;
    // 当前 key 是否进入 TopK
    private final boolean isHotKey;
    // 当前操作的 key
    private final String currentKey;

    public AddResult(String expelledKey, boolean isHotKey, String currentKey) {
        this.expelledKey = expelledKey;
        this.isHotKey = isHotKey;
        this.currentKey = currentKey;
    }

}
