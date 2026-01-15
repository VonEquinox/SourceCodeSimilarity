package DataStructure;

/**
 * 栈实现
 * 用于代码解析、括号匹配、嵌套深度计算等
 * @param <T> 泛型类型
 */
public class Stack<T> {

    private static final int DEFAULT_CAPACITY = 10;

    private Object[] elements;
    private int top;  // 栈顶指针

    public Stack() {
        this.elements = new Object[DEFAULT_CAPACITY];
        this.top = -1;
    }

    public Stack(int initialCapacity) {
        this.elements = new Object[initialCapacity];
        this.top = -1;
    }

    /**
     * 入栈
     */
    public void push(T element) {
        ensureCapacity();
        elements[++top] = element;
    }

    /**
     * 出栈
     */
    @SuppressWarnings("unchecked")
    public T pop() {
        if (isEmpty()) {
            throw new RuntimeException("Stack is empty");
        }
        T element = (T) elements[top];
        elements[top--] = null;
        return element;
    }

    /**
     * 查看栈顶元素
     */
    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) {
            throw new RuntimeException("Stack is empty");
        }
        return (T) elements[top];
    }

    public boolean isEmpty() {
        return top == -1;
    }

    public int size() {
        return top + 1;
    }

    public void clear() {
        while (top >= 0) {
            elements[top--] = null;
        }
    }

    private void ensureCapacity() {
        if (top == elements.length - 1) {
            int newCapacity = elements.length * 2;
            Object[] newElements = new Object[newCapacity];
            System.arraycopy(elements, 0, newElements, 0, elements.length);
            elements = newElements;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i <= top; i++) {
            sb.append(elements[i]);
            if (i < top) sb.append(", ");
        }
        sb.append("] <- top");
        return sb.toString();
    }
}
