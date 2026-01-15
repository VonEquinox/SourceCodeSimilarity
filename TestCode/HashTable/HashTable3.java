public class HashTable3 {
    private Node[] buckets;
    private int capacity;

    private class Node {
        int key, value;
        Node next;
        Node(int k, int v) { key = k; value = v; }
    }

    public HashTable3(int capacity) {
        this.capacity = capacity;
        buckets = new Node[capacity];
    }

    public void put(int key, int value) {
        int idx = Math.abs(key) % capacity;
        Node node = buckets[idx];
        while (node != null) {
            if (node.key == key) {
                node.value = value;
                return;
            }
            node = node.next;
        }
        Node newNode = new Node(key, value);
        newNode.next = buckets[idx];
        buckets[idx] = newNode;
    }

    public int get(int key) {
        int idx = Math.abs(key) % capacity;
        Node node = buckets[idx];
        while (node != null) {
            if (node.key == key) return node.value;
            node = node.next;
        }
        return -1;
    }
}
