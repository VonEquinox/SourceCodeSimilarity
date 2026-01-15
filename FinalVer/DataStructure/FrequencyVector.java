package DataStructure;

/**
 * 频度向量类
 * 用于存储关键字/标识符/运算符的频度向量
 * 包含相似度计算方法（欧氏距离、余弦相似度）
 */
public class FrequencyVector {

    private int[] data;      // 频度数据
    private int dimension;   // 向量维度

    public FrequencyVector(int dimension) {
        this.dimension = dimension;
        this.data = new int[dimension];
    }

    public FrequencyVector(int[] data) {
        this.dimension = data.length;
        this.data = new int[dimension];
        System.arraycopy(data, 0, this.data, 0, dimension);
    }

    public int get(int index) {
        if (index < 0 || index >= dimension) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
        return data[index];
    }

    public void set(int index, int value) {
        if (index < 0 || index >= dimension) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
        data[index] = value;
    }

    public void increment(int index) {
        if (index < 0 || index >= dimension) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
        data[index]++;
    }

    public int getDimension() {
        return dimension;
    }

    public int[] getData() {
        return data;
    }

    /**
     * 计算欧氏距离（课设基本要求）
     * s = sqrt(∑(xi1-xi2)²)
     */
    public static double euclideanDistance(FrequencyVector v1, FrequencyVector v2) {
        if (v1.dimension != v2.dimension) {
            throw new IllegalArgumentException("向量维度不一致");
        }
        double sum = 0.0;
        for (int i = 0; i < v1.dimension; i++) {
            int diff = v1.data[i] - v2.data[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    /**
     * 计算余弦相似度（增强方案）
     * cos(θ) = (X1·X2) / (|X1| × |X2|)
     * 返回值范围 [0, 1]，1表示完全相似
     */
    public static double cosineSimilarity(FrequencyVector v1, FrequencyVector v2) {
        if (v1.dimension != v2.dimension) {
            throw new IllegalArgumentException("向量维度不一致");
        }
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < v1.dimension; i++) {
            dotProduct += v1.data[i] * v2.data[i];
            norm1 += v1.data[i] * v1.data[i];
            norm2 += v2.data[i] * v2.data[i];
        }

        if (norm1 == 0 && norm2 == 0) return 1.0;
        if (norm1 == 0 || norm2 == 0) return 0.0;
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 计算向量的模（长度）
     */
    public double magnitude() {
        double sum = 0.0;
        for (int i = 0; i < dimension; i++) {
            sum += data[i] * data[i];
        }
        return Math.sqrt(sum);
    }

    /**
     * 计算向量元素总和
     */
    public int sum() {
        int total = 0;
        for (int i = 0; i < dimension; i++) {
            total += data[i];
        }
        return total;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < dimension; i++) {
            sb.append(data[i]);
            if (i < dimension - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}
