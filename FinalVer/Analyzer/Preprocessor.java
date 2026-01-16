package Analyzer;

/**
 * 代码预处理器
 * 去除注释、字符串字面量等干扰内容
 */
public class Preprocessor {

    /**
     * 预处理源代码
     */
    public static String process(String code) {
        code = removeComments(code);
        code = removeStringLiterals(code);
        code = removeCharLiterals(code);
        return code;
    }

    /**
     * 去除注释
     * 
     * 算法实现逻辑：
     * 采用简单的状态机思路遍历字符流，识别以下四种状态：
     * 1. 单行注释 (//...)：遇到 // 开始，直到换行符结束。
     * 2. 多行注释 (/*...* /)：遇到 /* 开始，直到 * / 结束。
     * 3. 字符串字面量 ("...")：在字符串内部时，忽略其中的 // 或 /*，防止误删。
     * 4. 字符字面量 ('...')：同上，处理单引号内的内容。
     * 
     * 目的：
     * 注释不参与逻辑运算，去除注释可以消除因注释内容不同而导致的相似度干扰。
     */
    private static String removeComments(String code) {
        StringBuilder result = new StringBuilder();
        boolean inString = false;      // 是否在双引号字符串内
        boolean inChar = false;        // 是否在单引号字符内
        boolean inLineComment = false; // 是否在单行注释内
        boolean inBlockComment = false;// 是否在块注释内
        int i = 0;
        while (i < code.length()) {
            char c = code.charAt(i);

            // 处理单行注释状态
            if (inLineComment) {
                if (c == '\n') {
                    inLineComment = false;
                    result.append('\n'); // 保留换行符以维持行号结构（可选）
                }
                i++;
                continue;
            }

            // 处理块注释状态
            if (inBlockComment) {
                if (c == '\n') {
                    result.append('\n');
                    i++;
                    continue;
                }
                // 检查块注释结束标志 */
                if (c == '*' && i + 1 < code.length() && code.charAt(i + 1) == '/') {
                    inBlockComment = false;
                    i += 2;
                    continue;
                }
                i++;
                continue;
            }

            // 处理字符串内部状态（需处理转义字符 \"）
            if (inString) {
                result.append(c);
                if (c == '\\' && i + 1 < code.length()) {
                    result.append(code.charAt(i + 1));
                    i += 2;
                    continue;
                }
                if (c == '"') {
                    inString = false;
                }
                i++;
                continue;
            }

            // 处理字符内部状态（需处理转义字符 \'）
            if (inChar) {
                result.append(c);
                if (c == '\\' && i + 1 < code.length()) {
                    result.append(code.charAt(i + 1));
                    i += 2;
                    continue;
                }
                if (c == '\'') {
                    inChar = false;
                }
                i++;
                continue;
            }

            // 状态切换检测
            if (c == '"') {
                inString = true;
                result.append(c);
                i++;
                continue;
            }
            if (c == '\'') {
                inChar = true;
                result.append(c);
                i++;
                continue;
            }

            // 检查注释开始标志 // 或 /*
            if (c == '/' && i + 1 < code.length()) {
                char next = code.charAt(i + 1);
                if (next == '/') {
                    inLineComment = true;
                    result.append(' '); // 用空格替换注释开始符
                    i += 2;
                    continue;
                }
                if (next == '*') {
                    inBlockComment = true;
                    result.append(' ');
                    i += 2;
                    continue;
                }
            }

            // 普通代码字符，直接保留
            result.append(c);
            i++;
        }
        return result.toString();
    }

    /**
     * 去除字符串字面量 "..."
     */
    private static String removeStringLiterals(String code) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < code.length()) {
            if (code.charAt(i) == '"') {
                result.append('"');
                i++;
                while (i < code.length() && code.charAt(i) != '"') {
                    if (code.charAt(i) == '\\' && i + 1 < code.length()) {
                        i += 2;
                    } else {
                        i++;
                    }
                }
                if (i < code.length()) {
                    result.append('"');
                    i++;
                }
            } else {
                result.append(code.charAt(i));
                i++;
            }
        }
        return result.toString();
    }

    /**
     * 去除字符字面量 '...'
     */
    private static String removeCharLiterals(String code) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < code.length()) {
            if (code.charAt(i) == '\'') {
                result.append('\'');
                i++;
                while (i < code.length() && code.charAt(i) != '\'') {
                    if (code.charAt(i) == '\\' && i + 1 < code.length()) {
                        i += 2;
                    } else {
                        i++;
                    }
                }
                if (i < code.length()) {
                    result.append('\'');
                    i++;
                }
            } else {
                result.append(code.charAt(i));
                i++;
            }
        }
        return result.toString();
    }
}
