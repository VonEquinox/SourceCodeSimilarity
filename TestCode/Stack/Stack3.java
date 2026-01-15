public class Stack3 {
    private Node top;
    private int size;

    private class Node {
        int data;
        Node next;
        Node(int data, Node next) {
            this.data = data;
            this.next = next;
        }
    }

    public void push(int value) {
        top = new Node(value, top);
        size++;
    }

    public int pop() {
        int value = top.data;
        top = top.next;
        size--;
        return value;
    }

    public int peek() {
        return top.data;
    }

    public boolean isEmpty() {
        return top == null;
    }

    public int size() {
        return size;
    }
}
