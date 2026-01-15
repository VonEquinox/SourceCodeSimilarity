package DataStructure;

/**
 * 哈希集合实现
 * 用于存储不重复的关键字、运算符等
 * 基于HashMap实现，元素类型固定为String
 */
public class HashSet {

    private HashMap<Object> map;
    private static final Object PRESENT = new Object();

    public HashSet() {
        map = new HashMap<>();
    }

    public HashSet(int initialCapacity) {
        map = new HashMap<>(initialCapacity);
    }

    /**
     * 添加元素
     * @return 如果元素不存在则返回true，已存在返回false
     */
    public boolean add(String element) {
        if (map.containsKey(element)) {
            return false;
        }
        map.put(element, PRESENT);
        return true;
    }

    /**
     * 删除元素
     */
    public boolean remove(String element) {
        return map.remove(element) != null;
    }

    /**
     * 检查元素是否存在
     */
    public boolean contains(String element) {
        return map.containsKey(element);
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public void clear() {
        map.clear();
    }

    /**
     * 获取所有元素
     */
    public String[] toArray() {
        return map.keys();
    }

    @Override
    public String toString() {
        String[] keys = map.keys();
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < keys.length; i++) {
            sb.append(keys[i]);
            if (i < keys.length - 1) sb.append(", ");
        }
        sb.append("}");
        return sb.toString();
    }
}
