public class LinkedList2 {
    private Element first;
    private int count;

    private class Element {
        int value;
        Element next;
        Element(int value) {
            this.value = value;
            this.next = null;
        }
    }

    public void append(int value) {
        Element newElement = new Element(value);
        if (first == null) {
            first = newElement;
        } else {
            Element curr = first;
            while (curr.next != null) {
                curr = curr.next;
            }
            curr.next = newElement;
        }
        count++;
    }

    public int fetch(int idx) {
        Element curr = first;
        for (int i = 0; i < idx; i++) {
            curr = curr.next;
        }
        return curr.value;
    }

    public void delete(int idx) {
        if (idx == 0) {
            first = first.next;
        } else {
            Element curr = first;
            for (int i = 0; i < idx - 1; i++) {
                curr = curr.next;
            }
            curr.next = curr.next.next;
        }
        count--;
    }

    public int length() {
        return count;
    }
}
