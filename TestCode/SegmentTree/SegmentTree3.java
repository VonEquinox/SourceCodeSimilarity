public class SegmentTree3 {
    private int[] tree;
    private int n;

    public SegmentTree3(int[] arr) {
        n = arr.length;
        tree = new int[2 * n];
        for (int i = 0; i < n; i++) {
            tree[n + i] = arr[i];
        }
        for (int i = n - 1; i > 0; i--) {
            tree[i] = tree[2 * i] + tree[2 * i + 1];
        }
    }

    public int query(int l, int r) {
        int sum = 0;
        l += n;
        r += n + 1;
        while (l < r) {
            if (l % 2 == 1) sum += tree[l++];
            if (r % 2 == 1) sum += tree[--r];
            l /= 2;
            r /= 2;
        }
        return sum;
    }

    public void update(int i, int val) {
        i += n;
        tree[i] = val;
        while (i > 1) {
            i /= 2;
            tree[i] = tree[2 * i] + tree[2 * i + 1];
        }
    }
}
