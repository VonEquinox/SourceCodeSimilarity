public class Heap4 {
    private int[] data;
    private int size;

    public Heap4(int capacity) {
        data = new int[capacity];
        size = 0;
    }

    public void insert(int value) {
        data[size] = value;
        siftUp(size);
        size++;
    }

    public int extract() {
        int max = data[0];
        data[0] = data[--size];
        siftDown(0);
        return max;
    }

    private void siftUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (data[index] <= data[parent]) break;
            swap(index, parent);
            index = parent;
        }
    }

    private void siftDown(int index) {
        while (index * 2 + 1 < size) {
            int child = index * 2 + 1;
            if (child + 1 < size && data[child + 1] > data[child]) {
                child++;
            }
            if (data[index] >= data[child]) break;
            swap(index, child);
            index = child;
        }
    }

    private void swap(int i, int j) {
        int temp = data[i];
        data[i] = data[j];
        data[j] = temp;
    }
}
