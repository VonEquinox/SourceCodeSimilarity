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
     * 去除注释（保留字符串/字符字面量，避免误删）
     */
    private static String removeComments(String code) {
        StringBuilder result = new StringBuilder();
        boolean inString = false;
        boolean inChar = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;
        int i = 0;
        while (i < code.length()) {
            char c = code.charAt(i);

            if (inLineComment) {
                if (c == '\n') {
                    inLineComment = false;
                    result.append('\n');
                }
                i++;
                continue;
            }

            if (inBlockComment) {
                if (c == '\n') {
                    result.append('\n');
                    i++;
                    continue;
                }
                if (c == '*' && i + 1 < code.length() && code.charAt(i + 1) == '/') {
                    inBlockComment = false;
                    i += 2;
                    continue;
                }
                i++;
                continue;
            }

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

            if (c == '/' && i + 1 < code.length()) {
                char next = code.charAt(i + 1);
                if (next == '/') {
                    inLineComment = true;
                    result.append(' ');
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
