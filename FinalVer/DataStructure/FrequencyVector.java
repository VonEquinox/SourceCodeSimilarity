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
     * 计算余弦相似度 (Cosine Similarity)
     * 
     * 数学公式：
     * cos(θ) = (v1 · v2) / (||v1|| * ||v2||)
     * 其中 (v1 · v2) 是向量点积，||v|| 是向量的模（欧几里得长度）。
     * 
     * 物理意义：
     * 余弦相似度衡量的是两个向量在空间中的“夹角”大小，而不是绝对长度。
     * 在代码分析中，这意味着它关注的是“特征分布的比例”是否一致。
     * 例如：代码 A 中 if 出现了 10 次，for 出现了 5 次；
     *       代码 B 中 if 出现了 20 次，for 出现了 10 次。
     * 尽管绝对次数不同，但它们的余弦相似度为 1.0（完全一致），这能有效处理代码扩写的情况。
     * 
     * @return 相似度值 [0, 1]，1 表示方向完全相同，0 表示完全正交（无共同特征）。
     */
    public static double cosineSimilarity(FrequencyVector v1, FrequencyVector v2) {
        if (v1.dimension != v2.dimension) {
            throw new IllegalArgumentException("向量维度不一致");
        }
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < v1.dimension; i++) {
            double a = v1.data[i];
            double b = v2.data[i];
            dotProduct += a * b;
            norm1 += a * a;
            norm2 += b * b;
        }

        // 处理全零向量的情况，防止除以零
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
