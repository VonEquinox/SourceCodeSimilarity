package DataStructure;

/**
 * 哈希表实现（链地址法解决冲突）
 * 用于统计关键字、标识符、运算符的频度
 * 键类型固定为String，使用双哈希函数
 * @param <V> 值类型
 */
public class HashMap<V> {

    private static final int DEFAULT_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75f;

    private HashEntry<String, V>[] buckets;  // 哈希桶数组
    private int size;                         // 元素数量
    private int capacity;                     // 桶容量

    @SuppressWarnings("unchecked")
    public HashMap() {
        this.capacity = DEFAULT_CAPACITY;
        this.buckets = new HashEntry[capacity];
        this.size = 0;
    }

    @SuppressWarnings("unchecked")
    public HashMap(int initialCapacity) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }
        this.capacity = initialCapacity;
        this.buckets = new HashEntry[capacity];
        this.size = 0;
    }

    /**
     * 双哈希函数 (Double Hashing Concept)
     * 
     * 设计思路：
     * 为了在不使用 Java 标准库的情况下保证哈希表的性能，我们使用了两个不同的质数（131 和 137）作为基数。
     * 1. h1 和 h2 分别计算字符串的哈希值。
     * 2. 将两个 32 位哈希值组合成一个 64 位的 long 值。
     * 
     * 这种方式极大地降低了不同字符串产生相同哈希索引（哈希碰撞）的概率，
     * 确保了在处理大量 Token（如 N-gram 组合）时，哈希表的查询效率接近 O(1)。
     */
    private long doubleHash(String key) {
        if (key == null) return 0;
        long h1 = 0, h2 = 0;
        for (int i = 0; i < key.length(); i++) {
            h1 = h1 * 131 + key.charAt(i);
            h2 = h2 * 137 + key.charAt(i);
        }
        return ((h1 & 0xFFFFFFFFL) << 32) | (h2 & 0xFFFFFFFFL);
    }

    /**
     * 计算哈希桶索引
     */
    private int hash(String key) {
        if (key == null) return 0;
        long h = doubleHash(key);
        // 取模运算定位到具体的桶，处理负数情况
        int index = (int) (h % capacity);
        return index < 0 ? index + capacity : index;
    }

    /**
     * 插入或更新键值对
     * 采用“链地址法”解决冲突：每个桶位是一个单向链表
     */
    public void put(String key, V value) {
        // 检查负载因子，必要时进行 2 倍扩容
        if ((float) size / capacity >= LOAD_FACTOR) {
            resize();
        }

        int index = hash(key);
        HashEntry<String, V> entry = buckets[index];

        // 遍历链表，寻找是否存在相同的键
        while (entry != null) {
            if (entry.key == null && key == null ||
                entry.key != null && entry.key.equals(key)) {
                entry.setValue(value); // 键已存在，更新值
                return;
            }
            entry = entry.next;
        }

        // 键不存在，创建新节点并插入到链表头部（头插法）
        HashEntry<String, V> newEntry = new HashEntry<>(key, value);
        newEntry.next = buckets[index];
        buckets[index] = newEntry;
        size++;
    }

    /**
     * 获取值
     */
    public V get(String key) {
        int index = hash(key);
        HashEntry<String, V> entry = buckets[index];

        while (entry != null) {
            if (entry.key == null && key == null ||
                entry.key != null && entry.key.equals(key)) {
                return entry.getValue();
            }
            entry = entry.next;
        }
        return null;
    }

    /**
     * 检查键是否存在
     */
    public boolean containsKey(String key) {
        return get(key) != null;
    }

    /**
     * 删除键值对
     */
    public V remove(String key) {
        int index = hash(key);
        HashEntry<String, V> entry = buckets[index];
        HashEntry<String, V> prev = null;

        while (entry != null) {
            if (entry.key == null && key == null ||
                entry.key != null && entry.key.equals(key)) {
                if (prev == null) {
                    buckets[index] = entry.next;
                } else {
                    prev.next = entry.next;
                }
                size--;
                return entry.getValue();
            }
            prev = entry;
            entry = entry.next;
        }
        return null;
    }

    /**
     * 扩容
     */
    @SuppressWarnings("unchecked")
    private void resize() {
        int newCapacity = capacity * 2;
        HashEntry<String, V>[] newBuckets = new HashEntry[newCapacity];
        int oldCapacity = capacity;
        HashEntry<String, V>[] oldBuckets = buckets;

        capacity = newCapacity;
        buckets = newBuckets;
        size = 0;

        for (int i = 0; i < oldCapacity; i++) {
            HashEntry<String, V> entry = oldBuckets[i];
            while (entry != null) {
                put(entry.key, entry.value);
                entry = entry.next;
            }
        }
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        for (int i = 0; i < capacity; i++) {
            buckets[i] = null;
        }
        size = 0;
    }

    /**
     * 获取所有键
     */
    public String[] keys() {
        String[] keys = new String[size];
        int idx = 0;
        for (int i = 0; i < capacity; i++) {
            HashEntry<String, V> entry = buckets[i];
            while (entry != null) {
                keys[idx++] = entry.key;
                entry = entry.next;
            }
        }
        return keys;
    }

    /**
     * 获取所有值
     */
    @SuppressWarnings("unchecked")
    public V[] values() {
        Object[] values = new Object[size];
        int idx = 0;
        for (int i = 0; i < capacity; i++) {
            HashEntry<String, V> entry = buckets[i];
            while (entry != null) {
                values[idx++] = entry.value;
                entry = entry.next;
            }
        }
        return (V[]) values;
    }

    /**
     * 获取或返回默认值
     */
    public V getOrDefault(String key, V defaultValue) {
        V value = get(key);
        return value != null ? value : defaultValue;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (int i = 0; i < capacity; i++) {
            HashEntry<String, V> entry = buckets[i];
            while (entry != null) {
                if (!first) sb.append(", ");
                sb.append(entry.key).append("=").append(entry.value);
                first = false;
                entry = entry.next;
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
