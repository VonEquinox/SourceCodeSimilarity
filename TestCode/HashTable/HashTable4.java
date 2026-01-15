public class HashTable4 {
    private int[] keys;
    private int[] values;
    private boolean[] used;
    private int capacity;

    public HashTable4(int capacity) {
        this.capacity = capacity;
        keys = new int[capacity];
        values = new int[capacity];
        used = new boolean[capacity];
    }

    public void put(int key, int value) {
        int index = hash(key);
        int i = 1;
        while (used[index] && keys[index] != key) {
            index = (index + i * i) % capacity;
            i++;
        }
        keys[index] = key;
        values[index] = value;
        used[index] = true;
    }

    public int get(int key) {
        int index = hash(key);
        int i = 1;
        while (used[index]) {
            if (keys[index] == key) {
                return values[index];
            }
            index = (index + i * i) % capacity;
            i++;
        }
        return -1;
    }

    private int hash(int key) {
        return Math.abs(key) % capacity;
    }
}
