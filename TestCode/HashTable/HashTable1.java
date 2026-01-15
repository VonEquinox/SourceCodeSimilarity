public class HashTable1 {
    private int[] keys;
    private int[] values;
    private boolean[] used;
    private int capacity;

    public HashTable1(int capacity) {
        this.capacity = capacity;
        keys = new int[capacity];
        values = new int[capacity];
        used = new boolean[capacity];
    }

    public void put(int key, int value) {
        int index = hash(key);
        while (used[index] && keys[index] != key) {
            index = (index + 1) % capacity;
        }
        keys[index] = key;
        values[index] = value;
        used[index] = true;
    }

    public int get(int key) {
        int index = hash(key);
        while (used[index]) {
            if (keys[index] == key) {
                return values[index];
            }
            index = (index + 1) % capacity;
        }
        return -1;
    }

    private int hash(int key) {
        return Math.abs(key) % capacity;
    }
}
