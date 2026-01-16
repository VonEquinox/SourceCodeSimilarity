import DataStructure.ArrayList;
import DataStructure.HashMap;
import DataStructure.FrequencyVector;
import Analyzer.*;
import Constants.JavaConstants;

/**
 * 相似度计算器
 * 核心入口：计算两个Java源代码的相似度
 */
public class SimilarityCalculator {

    // 权重配置
    // 由 `fit_weights.py --labels labels_all.json` 拟合得到（kw/id/op/seq/len，且权重和为1）
    private static final double KEYWORD_WEIGHT = 0.241617;
    private static final double IDENTIFIER_WEIGHT = 0.106786;
    private static final double OPERATOR_WEIGHT = 0.194093;
    private static final double SEQUENCE_WEIGHT = 0.176877;
    private static final double LENGTH_WEIGHT = 0.280627;

    /**
     * 计算两个源代码的相似度
     * @param code1 第一个源代码
     * @param code2 第二个源代码
     * @return 相似度值 (0.0 ~ 1.0)
     */
    public static double calculate(String code1, String code2) {
        // 1. 预处理
        String processed1 = Preprocessor.process(code1);
        String processed2 = Preprocessor.process(code2);

        // 2. 词法分析
        ArrayList<String> tokens1 = Lexer.tokenize(processed1);
        ArrayList<String> tokens2 = Lexer.tokenize(processed2);

        // 3. 关键字分析
        HashMap<Integer> kwMap1 = KeywordAnalyzer.analyze(tokens1);
        HashMap<Integer> kwMap2 = KeywordAnalyzer.analyze(tokens2);
        FrequencyVector kwVec1 = KeywordAnalyzer.toVector(kwMap1);
        FrequencyVector kwVec2 = KeywordAnalyzer.toVector(kwMap2);

        // 4. 运算符分析
        HashMap<Integer> opMap1 = OperatorAnalyzer.analyze(tokens1);
        HashMap<Integer> opMap2 = OperatorAnalyzer.analyze(tokens2);
        FrequencyVector opVec1 = OperatorAnalyzer.toVector(opMap1);
        FrequencyVector opVec2 = OperatorAnalyzer.toVector(opMap2);

        // 5. 标识符分析
        HashMap<Integer> idMap1 = IdentifierAnalyzer.analyzeNormalized(tokens1);
        HashMap<Integer> idMap2 = IdentifierAnalyzer.analyzeNormalized(tokens2);
        double idSim = cosineSimilaritySparseCounts(idMap1, idMap2);

        // 6. 计算各维度相似度
        double kwSim = FrequencyVector.cosineSimilarity(kwVec1, kwVec2);
        double opSim = FrequencyVector.cosineSimilarity(opVec1, opVec2);
        double seqSim = calculateNormalizedTokenNGramSimilarity(tokens1, tokens2);
        double lenSim = calculateTokenLengthSimilarity(tokens1, tokens2);

        // 7. 加权综合
        double similarity = KEYWORD_WEIGHT * kwSim
                          + IDENTIFIER_WEIGHT * idSim
                          + OPERATOR_WEIGHT * opSim
                          + SEQUENCE_WEIGHT * seqSim
                          + LENGTH_WEIGHT * lenSim;

        return similarity;
    }

    // 标识符相似度已改为：归一化 ID 频度的余弦相似度（见上方 idSim 计算）

    /**
     * 计算归一化 token n-gram 相似度（余弦）
     * - 将所有标识符统一映射为 ID，提升抗变量/方法名重命名能力
     * - 使用 bi-gram 与 tri-gram 做简单融合，兼顾短代码片段
     */
    private static double calculateNormalizedTokenNGramSimilarity(ArrayList<String> tokens1, ArrayList<String> tokens2) {
        // 更偏向长 n-gram：减少“通用 Java 模板”造成的虚高（尤其跨主题数据结构）。
        double s2 = cosineSimilaritySparseCounts(
            NGramAnalyzer.analyzeNormalizedNGrams(tokens1, 2),
            NGramAnalyzer.analyzeNormalizedNGrams(tokens2, 2)
        );
        double s3 = cosineSimilaritySparseCounts(
            NGramAnalyzer.analyzeNormalizedNGrams(tokens1, 3),
            NGramAnalyzer.analyzeNormalizedNGrams(tokens2, 3)
        );
        double s4 = cosineSimilaritySparseCounts(
            NGramAnalyzer.analyzeNormalizedNGrams(tokens1, 4),
            NGramAnalyzer.analyzeNormalizedNGrams(tokens2, 4)
        );
        double s5 = cosineSimilaritySparseCounts(
            NGramAnalyzer.analyzeNormalizedNGrams(tokens1, 5),
            NGramAnalyzer.analyzeNormalizedNGrams(tokens2, 5)
        );
        double s6 = cosineSimilaritySparseCounts(
            NGramAnalyzer.analyzeNormalizedNGrams(tokens1, 6),
            NGramAnalyzer.analyzeNormalizedNGrams(tokens2, 6)
        );
        double s7 = cosineSimilaritySparseCounts(
            NGramAnalyzer.analyzeNormalizedNGrams(tokens1, 7),
            NGramAnalyzer.analyzeNormalizedNGrams(tokens2, 7)
        );
        double s8 = cosineSimilaritySparseCounts(
            NGramAnalyzer.analyzeNormalizedNGrams(tokens1, 8),
            NGramAnalyzer.analyzeNormalizedNGrams(tokens2, 8)
        );

        return 0.05 * s2 + 0.08 * s3 + 0.10 * s4 + 0.12 * s5 + 0.15 * s6 + 0.20 * s7 + 0.30 * s8;
    }

    private static double cosineSimilaritySparseCounts(HashMap<Integer> map1, HashMap<Integer> map2) {
        String[] keys1 = map1.keys();
        String[] keys2 = map2.keys();

        if (keys1.length == 0 && keys2.length == 0) {
            return 1.0;
        }
        if (keys1.length == 0 || keys2.length == 0) {
            return 0.0;
        }

        HashMap<Integer> a = map1;
        HashMap<Integer> b = map2;
        String[] aKeys = keys1;
        String[] bKeys = keys2;
        if (keys1.length > keys2.length) {
            a = map2;
            b = map1;
            aKeys = keys2;
            bKeys = keys1;
        }

        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (String k : aKeys) {
            int av = a.getOrDefault(k, 0);
            int bv = b.getOrDefault(k, 0);
            dot += (double) av * bv;
            normA += (double) av * av;
        }

        for (String k : bKeys) {
            int bv = b.getOrDefault(k, 0);
            normB += (double) bv * bv;
        }

        if (normA == 0 && normB == 0) return 1.0;
        if (normA == 0 || normB == 0) return 0.0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * token 数量相似度（长度比）
     * 用于区分“结构规模差异”导致的相似度差异（例如方法数明显不同）。
     */
    private static double calculateTokenLengthSimilarity(ArrayList<String> tokens1, ArrayList<String> tokens2) {
        int n1 = tokens1.size();
        int n2 = tokens2.size();
        if (n1 == 0 && n2 == 0) {
            return 1.0;
        }
        if (n1 == 0 || n2 == 0) {
            return 0.0;
        }
        int min = Math.min(n1, n2);
        int max = Math.max(n1, n2);
        return (double) min / (double) max;
    }
}
