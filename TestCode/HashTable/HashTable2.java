public class HashTable2 {
    private int[] keyArr;
    private int[] valArr;
    private boolean[] occupied;
    private int size;

    public HashTable2(int size) {
        this.size = size;
        keyArr = new int[size];
        valArr = new int[size];
        occupied = new boolean[size];
    }

    public void insert(int k, int v) {
        int idx = hashCode(k);
        while (occupied[idx] && keyArr[idx] != k) {
            idx = (idx + 1) % size;
        }
        keyArr[idx] = k;
        valArr[idx] = v;
        occupied[idx] = true;
    }

    public int find(int k) {
        int idx = hashCode(k);
        while (occupied[idx]) {
            if (keyArr[idx] == k) {
                return valArr[idx];
            }
            idx = (idx + 1) % size;
        }
        return -1;
    }

    private int hashCode(int k) {
        return Math.abs(k) % size;
    }
}
