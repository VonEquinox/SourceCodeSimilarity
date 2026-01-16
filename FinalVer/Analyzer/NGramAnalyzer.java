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
     * 对给定 token 序列提取 n-gram 频次。
     * 
     * 原理：
     * N-gram 是一种基于滑动窗口的特征提取方法。
     * 对于序列 [A, B, C, D]，其 2-gram (n=2) 为：[A B], [B C], [C D]。
     * 
     * 作用：
     * 单个 Token（如 "if"）无法反映逻辑，但连续的 Token 序列（如 "if ( ID0 > ID1 )"）
     * 能极好地捕捉代码的局部控制流和运算结构。
     */
    public static HashMap<Integer> analyzeNGrams(ArrayList<String> tokens, int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("n must be positive: " + n);
        }

        HashMap<Integer> freqMap = new HashMap<>();
        if (tokens.size() < n) {
            return freqMap;
        }

        // 滑动窗口提取
        for (int i = 0; i + n <= tokens.size(); i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < n; j++) {
                if (j > 0) sb.append(' ');
                sb.append(tokens.get(i + j));
            }
            String gram = sb.toString();
            int count = freqMap.getOrDefault(gram, 0);
            freqMap.put(gram, count + 1);
        }

        return freqMap;
    }

    /**
     * 对 token 序列做归一化后提取 n-gram 频次。
     * 
     * 核心步骤：
     * 1. 标识符归一化：将所有自定义变量/方法名转换为 ID0, ID1...
     * 2. 过滤字面量：剔除 true/false/null 等对结构贡献较小的 Token。
     * 3. 提取 N-gram：在归一化后的序列上进行滑动窗口统计。
     * 
     * 优势：
     * 这种方法结合了“结构信息”和“抗重命名能力”，是检测高级抄袭（如改名、换变量顺序）的核心手段。
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
