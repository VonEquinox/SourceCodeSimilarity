public class Stack4 {
    private int[] data;
    private int top1;
    private int top2;

    public Stack4(int capacity) {
        data = new int[capacity];
        top1 = -1;
        top2 = capacity;
    }

    public void push1(int value) {
        data[++top1] = value;
    }

    public void push2(int value) {
        data[--top2] = value;
    }

    public int pop1() {
        return data[top1--];
    }

    public int pop2() {
        return data[top2++];
    }

    public boolean isEmpty1() {
        return top1 == -1;
    }

    public boolean isEmpty2() {
        return top2 == data.length;
    }
}
