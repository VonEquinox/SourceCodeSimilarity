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
     * 例：第一个出现的标识符 -> ID0，第二个 -> ID1，...
     * 目的：提高对纯重命名的鲁棒性，同时保留“哪个标识符更常用”的结构差异。
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
