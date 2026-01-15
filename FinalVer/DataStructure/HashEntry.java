package DataStructure;

/**
 * 哈希表键值对条目
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class HashEntry<K, V> {
    K key;
    V value;
    HashEntry<K, V> next;  // 链地址法解决冲突

    public HashEntry(K key, V value) {
        this.key = key;
        this.value = value;
        this.next = null;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }
}
