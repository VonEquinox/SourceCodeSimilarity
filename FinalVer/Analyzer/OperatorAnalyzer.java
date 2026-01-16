package Analyzer;

import DataStructure.ArrayList;
import DataStructure.HashMap;
import DataStructure.FrequencyVector;
import Constants.JavaConstants;

/**
 * 运算符分析器
 * 统计运算符出现频度
 */
public class OperatorAnalyzer {

    /**
     * 分析Token列表，统计运算符频度
     */
    public static HashMap<Integer> analyze(ArrayList<String> tokens) {
        HashMap<Integer> freqMap = new HashMap<>();

        // 初始化所有运算符频度为0
        for (String op : JavaConstants.OPERATORS) {
            freqMap.put(op, 0);
        }

        // 统计运算符频度
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (JavaConstants.isOperator(token)) {
                if (isDelimiter(token)) {
                    continue;
                }
                int count = freqMap.getOrDefault(token, 0);
                freqMap.put(token, count + 1);
            }
        }

        return freqMap;
    }

    private static boolean isDelimiter(String token) {
        return "(".equals(token) || ")".equals(token)
            || "{".equals(token) || "}".equals(token)
            || "[".equals(token) || "]".equals(token)
            || ";".equals(token) || ",".equals(token)
            || ".".equals(token) || "@".equals(token);
    }

    /**
     * 将频度Map转换为向量
     */
    public static FrequencyVector toVector(HashMap<Integer> freqMap) {
        FrequencyVector vector = new FrequencyVector(JavaConstants.OPERATORS.length);
        for (int i = 0; i < JavaConstants.OPERATORS.length; i++) {
            Integer count = freqMap.get(JavaConstants.OPERATORS[i]);
            vector.set(i, count != null ? count : 0);
        }
        return vector;
    }
}
