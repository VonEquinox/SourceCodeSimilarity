public class Heap3 {
    private int[] data;
    private int size;

    public Heap3(int capacity) {
        data = new int[capacity];
    }

    public void insert(int value) {
        data[size++] = value;
        heapifyUp(size - 1);
    }

    public int extract() {
        int min = data[0];
        data[0] = data[--size];
        heapifyDown(0);
        return min;
    }

    private void heapifyUp(int i) {
        if (i == 0) return;
        int p = (i - 1) / 2;
        if (data[i] < data[p]) {
            swap(i, p);
            heapifyUp(p);
        }
    }

    private void heapifyDown(int i) {
        int smallest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;
        if (left < size && data[left] < data[smallest]) smallest = left;
        if (right < size && data[right] < data[smallest]) smallest = right;
        if (smallest != i) {
            swap(i, smallest);
            heapifyDown(smallest);
        }
    }

    private void swap(int i, int j) {
        int t = data[i]; data[i] = data[j]; data[j] = t;
    }
}
