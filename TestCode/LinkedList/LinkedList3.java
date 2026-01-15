public class LinkedList3 {
    private Node head;
    private Node tail;
    private int size;

    private class Node {
        int data;
        Node next;
        Node(int data) { this.data = data; }
    }

    public void add(int data) {
        Node node = new Node(data);
        if (tail == null) {
            head = tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        size++;
    }

    public int get(int index) {
        return getNode(index).data;
    }

    private Node getNode(int index) {
        Node n = head;
        while (index-- > 0) n = n.next;
        return n;
    }

    public void remove(int index) {
        if (index == 0) {
            head = head.next;
            if (head == null) tail = null;
        } else {
            Node prev = getNode(index - 1);
            prev.next = prev.next.next;
            if (prev.next == null) tail = prev;
        }
        size--;
    }

    public int size() { return size; }
}
