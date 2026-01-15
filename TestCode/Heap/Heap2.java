public class Heap2 {
    private int[] elements;
    private int count;

    public Heap2(int maxSize) {
        elements = new int[maxSize];
        count = 0;
    }

    public void add(int val) {
        elements[count] = val;
        bubbleUp(count);
        count++;
    }

    public int poll() {
        int min = elements[0];
        elements[0] = elements[--count];
        bubbleDown(0);
        return min;
    }

    private void bubbleUp(int idx) {
        while (idx > 0) {
            int p = (idx - 1) / 2;
            if (elements[idx] >= elements[p]) break;
            exchange(idx, p);
            idx = p;
        }
    }

    private void bubbleDown(int idx) {
        while (idx * 2 + 1 < count) {
            int c = idx * 2 + 1;
            if (c + 1 < count && elements[c + 1] < elements[c]) {
                c++;
            }
            if (elements[idx] <= elements[c]) break;
            exchange(idx, c);
            idx = c;
        }
    }

    private void exchange(int a, int b) {
        int t = elements[a];
        elements[a] = elements[b];
        elements[b] = t;
    }
}
