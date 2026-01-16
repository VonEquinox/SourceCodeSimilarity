import DataStructure.ArrayList;
import DataStructure.HashMap;
import DataStructure.FrequencyVector;
import Analyzer.*;
import Constants.JavaConstants;

/**
 * 相似度计算器
 * 核心入口：计算两个Java源代码的相似度
 * 
 * 该类采用多维度加权融合算法：
 * 1. 关键字频度 (KEYWORD)：反映编程语言基础构件的使用分布。
 * 2. 标识符序列 (IDENTIFIER)：反映变量和函数调用的逻辑顺序，抗重命名。
 * 3. 运算符频度 (OPERATOR)：反映计算逻辑和控制流的复杂度。
 * 4. Token序列 (SEQUENCE)：通过N-gram捕捉局部代码块的结构特征。
 * 5. 代码长度 (LENGTH)：衡量两份代码在规模上的对等性。
 */
public class SimilarityCalculator {

    // 权重配置：由 fit_weights_torch.py 训练得到，总和为1
    private static final double KEYWORD_WEIGHT = 0.368208;
    private static final double IDENTIFIER_WEIGHT = 0.055308;
    private static final double OPERATOR_WEIGHT = 0.118788;
    private static final double SEQUENCE_WEIGHT = 0.148921;
    private static final double LENGTH_WEIGHT = 0.308775;

    /**
     * 计算两个源代码的相似度
     * @param code1 第一个源代码
     * @param code2 第二个源代码
     * @return 相似度值 (0.0 ~ 1.0)
     */
    public static double calculate(String code1, String code2) {
        // 1. 预处理：去除注释、字符串字面量等噪声
        String processed1 = Preprocessor.process(code1);
        String processed2 = Preprocessor.process(code2);

        // 2. 词法分析：将代码切分为Token流
        ArrayList<String> tokens1 = Lexer.tokenize(processed1);
        ArrayList<String> tokens2 = Lexer.tokenize(processed2);

        // 3. 关键字分析：统计Java关键字频次并向量化
        HashMap<Integer> kwMap1 = KeywordAnalyzer.analyze(tokens1);
        HashMap<Integer> kwMap2 = KeywordAnalyzer.analyze(tokens2);
        FrequencyVector kwVec1 = KeywordAnalyzer.toVector(kwMap1);
        FrequencyVector kwVec2 = KeywordAnalyzer.toVector(kwMap2);

        // 4. 运算符分析：统计运算符频次并向量化
        HashMap<Integer> opMap1 = OperatorAnalyzer.analyze(tokens1);
        HashMap<Integer> opMap2 = OperatorAnalyzer.analyze(tokens2);
        FrequencyVector opVec1 = OperatorAnalyzer.toVector(opMap1);
        FrequencyVector opVec2 = OperatorAnalyzer.toVector(opMap2);

        // 5. 标识符序列分析：提取归一化后的标识符流并计算结构相似度
        double idSim = calculateIdentifierSequenceSimilarity(tokens1, tokens2);

        // 6. 计算各维度相似度：使用余弦相似度衡量向量间的分布一致性
        double kwSim = FrequencyVector.cosineSimilarity(kwVec1, kwVec2);
        double opSim = FrequencyVector.cosineSimilarity(opVec1, opVec2);
        double seqSim = calculateNormalizedTokenNGramSimilarity(tokens1, tokens2);
        double lenSim = calculateTokenLengthSimilarity(tokens1, tokens2);

        // 7. 加权综合：将各维度得分按权重累加得到最终结果
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

    /**
     * 标识符序列结构相似度：
     * 1) 从 token 流中提取标识符序列
     * 2) 按首次出现顺序归一化为 ID0/ID1/...
     * 3) 对 ID 序列做 n-gram 余弦（偏向长 n-gram）
     *
     * 这样对“纯重命名”不敏感，同时能更好区分不同实现的标识符使用模式。
     */
    private static double calculateIdentifierSequenceSimilarity(ArrayList<String> tokens1, ArrayList<String> tokens2) {
        ArrayList<String> ids1 = IdentifierAnalyzer.normalizedIdentifierSequence(tokens1);
        ArrayList<String> ids2 = IdentifierAnalyzer.normalizedIdentifierSequence(tokens2);

        double s2 = cosineSimilaritySparseCounts(NGramAnalyzer.analyzeNGrams(ids1, 2), NGramAnalyzer.analyzeNGrams(ids2, 2));
        double s3 = cosineSimilaritySparseCounts(NGramAnalyzer.analyzeNGrams(ids1, 3), NGramAnalyzer.analyzeNGrams(ids2, 3));
        double s4 = cosineSimilaritySparseCounts(NGramAnalyzer.analyzeNGrams(ids1, 4), NGramAnalyzer.analyzeNGrams(ids2, 4));
        double s5 = cosineSimilaritySparseCounts(NGramAnalyzer.analyzeNGrams(ids1, 5), NGramAnalyzer.analyzeNGrams(ids2, 5));
        double s6 = cosineSimilaritySparseCounts(NGramAnalyzer.analyzeNGrams(ids1, 6), NGramAnalyzer.analyzeNGrams(ids2, 6));

        return 0.10 * s2 + 0.15 * s3 + 0.20 * s4 + 0.25 * s5 + 0.30 * s6;
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
