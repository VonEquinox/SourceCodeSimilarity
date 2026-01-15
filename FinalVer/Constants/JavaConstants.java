package Constants;

import DataStructure.HashSet;

/**
 * Java语言常量定义
 * 包含关键字、运算符等
 */
public class JavaConstants {

    // Java 50个关键字
    public static final String[] KEYWORDS = {
        "abstract", "assert", "boolean", "break", "byte",
        "case", "catch", "char", "class", "const",
        "continue", "default", "do", "double", "else",
        "enum", "extends", "final", "finally", "float",
        "for", "goto", "if", "implements", "import",
        "instanceof", "int", "interface", "long", "native",
        "new", "package", "private", "protected", "public",
        "return", "short", "static", "strictfp", "super",
        "switch", "synchronized", "this", "throw", "throws",
        "transient", "try", "void", "volatile", "while"
    };

    // 运算符列表
    public static final String[] OPERATORS = {
        // 算术运算符
        "+", "-", "*", "/", "%", "++", "--",
        // 关系运算符
        "==", "!=", ">", "<", ">=", "<=",
        // 逻辑运算符
        "&&", "||", "!",
        // 位运算符
        "&", "|", "^", "~", "<<", ">>", ">>>",
        // 赋值运算符
        "=", "+=", "-=", "*=", "/=", "%=",
        "&=", "|=", "^=", "<<=", ">>=", ">>>=",
        // 其他
        "?", ":"
    };

    // 使用HashSet加速查询
    private static HashSet keywordSet;
    private static HashSet operatorSet;

    static {
        keywordSet = new HashSet();
        for (String keyword : KEYWORDS) {
            keywordSet.add(keyword);
        }
        operatorSet = new HashSet();
        for (String operator : OPERATORS) {
            operatorSet.add(operator);
        }
    }

    /**
     * 判断是否为关键字
     */
    public static boolean isKeyword(String word) {
        return keywordSet.contains(word);
    }

    /**
     * 判断是否为运算符
     */
    public static boolean isOperator(String op) {
        return operatorSet.contains(op);
    }

    /**
     * 获取关键字索引
     */
    public static int getKeywordIndex(String keyword) {
        for (int i = 0; i < KEYWORDS.length; i++) {
            if (KEYWORDS[i].equals(keyword)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 获取运算符索引
     */
    public static int getOperatorIndex(String operator) {
        for (int i = 0; i < OPERATORS.length; i++) {
            if (OPERATORS[i].equals(operator)) {
                return i;
            }
        }
        return -1;
    }
}
