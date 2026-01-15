package Analyzer;

import DataStructure.ArrayList;
import DataStructure.HashMap;
import DataStructure.FrequencyVector;
import Constants.JavaConstants;

/**
 * 关键字分析器
 * 统计Java关键字出现频度
 */
public class KeywordAnalyzer {

    /**
     * 分析Token列表，统计关键字频度
     */
    public static HashMap<Integer> analyze(ArrayList<String> tokens) {
        HashMap<Integer> freqMap = new HashMap<>();

        // 初始化所有关键字频度为0
        for (String keyword : JavaConstants.KEYWORDS) {
            freqMap.put(keyword, 0);
        }

        // 统计关键字频度
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (JavaConstants.isKeyword(token)) {
                int count = freqMap.getOrDefault(token, 0);
                freqMap.put(token, count + 1);
            }
        }

        return freqMap;
    }

    /**
     * 将频度Map转换为向量
     */
    public static FrequencyVector toVector(HashMap<Integer> freqMap) {
        FrequencyVector vector = new FrequencyVector(JavaConstants.KEYWORDS.length);
        for (int i = 0; i < JavaConstants.KEYWORDS.length; i++) {
            Integer count = freqMap.get(JavaConstants.KEYWORDS[i]);
            vector.set(i, count != null ? count : 0);
        }
        return vector;
    }
}