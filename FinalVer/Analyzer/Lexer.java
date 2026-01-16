package Analyzer;

import DataStructure.ArrayList;
import Constants.JavaConstants;

/**
 * 词法分析器
 * 将源代码分解为Token序列
 */
public class Lexer {

    /**
     * 对源代码进行词法分析
     * @param code 预处理后的源代码
     * @return Token列表
     */
    public static ArrayList<String> tokenize(String code) {
        ArrayList<String> tokens = new ArrayList<>();
        int i = 0;

        // 遍历整个源代码字符串
        while (i < code.length()) {
            char c = code.charAt(i);

            // 1. 跳过空白字符（空格、换行、制表符等）
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            // 2. 识别标识符或关键字
            // Java 标识符必须以字母或下划线开头
            if (Character.isLetter(c) || c == '_') {
                StringBuilder sb = new StringBuilder();
                while (i < code.length() && (Character.isLetterOrDigit(code.charAt(i)) || code.charAt(i) == '_')) {
                    sb.append(code.charAt(i));
                    i++;
                }
                tokens.append(sb.toString());
                continue;
            }

            // 3. 识别数字字面量
            // 相似度分析中通常不关心具体数值，因此扫描后跳过，不存入 Token 列表
            if (Character.isDigit(c)) {
                while (i < code.length() && (Character.isDigit(code.charAt(i)) || code.charAt(i) == '.')) {
                    i++;
                }
                continue;
            }

            // 4. 尝试匹配运算符（如 ++, +=, ==, -> 等）
            // 采用“最长匹配”原则，优先匹配 3 字符和 2 字符的运算符
            String op = tryMatchOperator(code, i);
            if (op != null) {
                tokens.append(op);
                i += op.length();
                continue;
            }

            // 5. 忽略无法识别的单个字符（如非法符号）
            i++;
        }

        return tokens;
    }

    /**
     * 尝试匹配运算符（最长匹配）
     */
    private static String tryMatchOperator(String code, int start) {
        // 尝试匹配3字符运算符
        if (start + 3 <= code.length()) {
            String op3 = code.substring(start, start + 3);
            if (JavaConstants.isOperator(op3)) {
                return op3;
            }
        }
        // 尝试匹配2字符运算符
        if (start + 2 <= code.length()) {
            String op2 = code.substring(start, start + 2);
            if (JavaConstants.isOperator(op2)) {
                return op2;
            }
        }
        // 尝试匹配1字符运算符
        String op1 = code.substring(start, start + 1);
        if (JavaConstants.isOperator(op1)) {
            return op1;
        }
        return null;
    }
}