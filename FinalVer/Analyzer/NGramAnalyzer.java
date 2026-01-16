package Analyzer;

import Constants.JavaConstants;
import DataStructure.ArrayList;
import DataStructure.HashMap;

/**
 * N-gram 分析器（基于 token 序列）
 * 将标识符归一化为按“首次出现顺序”编号的 IDk，用于提升抗重命名能力，并保留局部结构差异。
 */
public class NGramAnalyzer {

    /**
     * 对 token 序列做归一化后提取 n-gram 频次。
     * - 关键字/运算符：原样保留
     * - 标识符：按“文件内首次出现顺序”映射为 "ID0","ID1",...
     * - true/false/null：剔除（不纳入相似度维度）
     */
    public static HashMap<Integer> analyzeNormalizedNGrams(ArrayList<String> tokens, int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("n must be positive: " + n);
        }

        HashMap<Integer> idMap = new HashMap<>();
        int nextId = 0;

        ArrayList<String> normalized = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (token == null || token.isEmpty()) continue;

            if (JavaConstants.isKeyword(token) || JavaConstants.isOperator(token)) {
                normalized.append(token);
                continue;
            }

            if (isExcludedLiteral(token)) {
                continue;
            }

            Integer id = idMap.get(token);
            if (id == null) {
                id = nextId++;
                idMap.put(token, id);
            }
            normalized.append("ID" + id);
        }

        HashMap<Integer> freqMap = new HashMap<>();
        if (normalized.size() < n) {
            return freqMap;
        }

        for (int i = 0; i + n <= normalized.size(); i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < n; j++) {
                if (j > 0) sb.append(' ');
                sb.append(normalized.get(i + j));
            }
            String gram = sb.toString();
            int count = freqMap.getOrDefault(gram, 0);
            freqMap.put(gram, count + 1);
        }

        return freqMap;
    }

    private static boolean isExcludedLiteral(String token) {
        return "true".equals(token) || "false".equals(token) || "null".equals(token);
    }
}
