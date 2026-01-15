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
    private static final double KEYWORD_WEIGHT = 0.35;
    private static final double IDENTIFIER_WEIGHT = 0.40;
    private static final double OPERATOR_WEIGHT = 0.25;

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
        HashMap<Integer> idMap1 = IdentifierAnalyzer.analyze(tokens1);
        HashMap<Integer> idMap2 = IdentifierAnalyzer.analyze(tokens2);
        double idSim = calculateIdentifierSimilarity(idMap1, idMap2);

        // 6. 计算各维度相似度
        double kwSim = FrequencyVector.cosineSimilarity(kwVec1, kwVec2);
        double opSim = FrequencyVector.cosineSimilarity(opVec1, opVec2);

        // 7. 加权综合
        double similarity = KEYWORD_WEIGHT * kwSim
                          + IDENTIFIER_WEIGHT * idSim
                          + OPERATOR_WEIGHT * opSim;

        return similarity;
    }

    /**
     * 计算标识符相似度（Jaccard系数）
     */
    private static double calculateIdentifierSimilarity(HashMap<Integer> map1, HashMap<Integer> map2) {
        String[] keys1 = map1.keys();
        String[] keys2 = map2.keys();

        if (keys1.length == 0 && keys2.length == 0) {
            return 1.0;
        }
        if (keys1.length == 0 || keys2.length == 0) {
            return 0.0;
        }

        // 计算交集大小
        int intersection = 0;
        for (String key : keys1) {
            if (map2.containsKey(key)) {
                intersection++;
            }
        }

        // Jaccard系数 = 交集 / 并集
        int union = keys1.length + keys2.length - intersection;
        return (double) intersection / union;
    }
}