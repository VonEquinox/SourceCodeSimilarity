import java.io.*;

/**
 * 测试入口
 * 读取两个Java文件并计算相似度
 */
public class Main {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("用法: java Main <file1.java> <file2.java>");
            return;
        }

        try {
            String code1 = readFile(args[0]);
            String code2 = readFile(args[1]);

            double similarity = SimilarityCalculator.calculate(code1, code2);

            System.out.println("文件1: " + args[0]);
            System.out.println("文件2: " + args[1]);
            System.out.printf("相似度: %.4f (%.2f%%)\n", similarity, similarity * 100);

        } catch (IOException e) {
            System.out.println("读取文件失败: " + e.getMessage());
        }
    }

    /**
     * 读取文件内容
     */
    private static String readFile(String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }
}