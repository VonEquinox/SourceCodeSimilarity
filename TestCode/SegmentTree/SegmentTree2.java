public class SegmentTree2 {
    private int[] data;
    private int size;

    public SegmentTree2(int[] arr) {
        size = arr.length;
        data = new int[4 * size];
        construct(arr, 1, 0, size - 1);
    }

    private void construct(int[] arr, int idx, int lo, int hi) {
        if (lo == hi) {
            data[idx] = arr[lo];
        } else {
            int m = (lo + hi) / 2;
            construct(arr, 2 * idx, lo, m);
            construct(arr, 2 * idx + 1, m + 1, hi);
            data[idx] = data[2 * idx] + data[2 * idx + 1];
        }
    }

    public int sum(int left, int right) {
        return sum(1, 0, size - 1, left, right);
    }

    private int sum(int idx, int lo, int hi, int left, int right) {
        if (right < lo || hi < left) return 0;
        if (left <= lo && hi <= right) return data[idx];
        int m = (lo + hi) / 2;
        return sum(2 * idx, lo, m, left, right)
             + sum(2 * idx + 1, m + 1, hi, left, right);
    }
}
