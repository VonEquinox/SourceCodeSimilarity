public class Stack2 {
    private int[] elements;
    private int pointer;

    public Stack2(int maxSize) {
        elements = new int[maxSize];
        pointer = -1;
    }

    public void insert(int val) {
        elements[++pointer] = val;
    }

    public int remove() {
        return elements[pointer--];
    }

    public int top() {
        return elements[pointer];
    }

    public boolean empty() {
        return pointer == -1;
    }

    public int count() {
        return pointer + 1;
    }
}
