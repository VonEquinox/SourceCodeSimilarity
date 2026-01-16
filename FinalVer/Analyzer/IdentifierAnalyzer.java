package Analyzer;

import DataStructure.ArrayList;
import DataStructure.HashMap;
import Constants.JavaConstants;

/**
 * 标识符分析器
 * 统计用户自定义标识符出现频度
 */
public class IdentifierAnalyzer {

    /**
     * 分析Token列表，统计用户标识符频度
     */
    public static HashMap<Integer> analyze(ArrayList<String> tokens) {
        HashMap<Integer> freqMap = new HashMap<>();

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            // 排除关键字和运算符，只统计用户标识符
            if (isIdentifier(token)) {
                int count = freqMap.getOrDefault(token, 0);
                freqMap.put(token, count + 1);
            }
        }

        return freqMap;
    }

    /**
     * 对标识符做“按首次出现顺序编号”的归一化，并统计频度。
     * 
     * 算法原理：
     * 1. 维护一个 idMap，记录“原变量名”到“流水号 IDk”的映射。
     * 2. 遍历 Token 流，遇到第一个新变量 a，映射为 ID0；遇到第二个新变量 b，映射为 ID1。
     * 3. 再次遇到 a 时，依然映射为 ID0。
     * 
     * 目的：
     * 这种映射方式使得代码逻辑结构（如：变量定义、赋值、循环引用）被抽象为 ID 序列，
     * 即使攻击者将所有变量名全局替换，生成的 ID 序列依然完全一致，从而实现极强的抗重命名能力。
     */
    public static HashMap<Integer> analyzeNormalized(ArrayList<String> tokens) {
        HashMap<Integer> freqMap = new HashMap<>();
        HashMap<Integer> idMap = new HashMap<>();
        int nextId = 0;

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (!isIdentifier(token)) {
                continue;
            }

            Integer existing = idMap.get(token);
            if (existing == null) {
                // 发现新标识符，分配下一个流水号
                existing = nextId++;
                idMap.put(token, existing);
            }

            String normalized = "ID" + existing;
            int count = freqMap.getOrDefault(normalized, 0);
            freqMap.put(normalized, count + 1);
        }

        return freqMap;
    }

    /**
     * 提取“标识符 token 序列”，并按首次出现顺序归一化为 ID0/ID1/...
     * 例：a b a c -> ID0 ID1 ID0 ID2
     */
    public static ArrayList<String> normalizedIdentifierSequence(ArrayList<String> tokens) {
        ArrayList<String> seq = new ArrayList<>();
        HashMap<Integer> idMap = new HashMap<>();
        int nextId = 0;

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (!isIdentifier(token)) {
                continue;
            }
            Integer existing = idMap.get(token);
            if (existing == null) {
                existing = nextId++;
                idMap.put(token, existing);
            }
            seq.append("ID" + existing);
        }

        return seq;
    }

    /**
     * 判断是否为用户标识符
     */
    private static boolean isIdentifier(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        // 排除关键字
        if (JavaConstants.isKeyword(token)) {
            return false;
        }
        // 排除运算符
        if (JavaConstants.isOperator(token)) {
            return false;
        }
        // 排除字面量
        if (isLiteral(token)) {
            return false;
        }
        // 必须以字母或下划线开头
        char first = token.charAt(0);
        if (!Character.isLetter(first) && first != '_') {
            return false;
        }
        return true;
    }

    private static boolean isLiteral(String token) {
        return "true".equals(token) || "false".equals(token) || "null".equals(token);
    }
}
