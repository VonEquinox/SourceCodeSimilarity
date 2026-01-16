import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import Analyzer.IdentifierAnalyzer;
import Analyzer.KeywordAnalyzer;
import Analyzer.Lexer;
import Analyzer.NGramAnalyzer;
import Analyzer.OperatorAnalyzer;
import Analyzer.Preprocessor;
import Constants.JavaConstants;
import DataStructure.ArrayList;
import DataStructure.FrequencyVector;
import DataStructure.HashMap;

/**
 * 导出相似度各维度特征（用于调参/拟合权重）
 *
 * 用法：
 *   java FeatureDump <file1.java> <file2.java>
 *
 * 输出（单行 JSON）：
 *   {"kw":...,"id":...,"op":...,"seq":...,"total":...}
 */
public class FeatureDump {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("用法: java FeatureDump <file1.java> <file2.java>");
            return;
        }

        String code1 = readFile(args[0]);
        String code2 = readFile(args[1]);

        Features features = computeFeatures(code1, code2);
        double total = SimilarityCalculator.calculate(code1, code2);

        System.out.printf(
            "{\"kw\":%.10f,\"id\":%.10f,\"op\":%.10f,\"seq\":%.10f,\"len\":%.10f,\"t1\":%d,\"t2\":%d,\"seq2\":%.10f,\"seq3\":%.10f,\"seq4\":%.10f,\"seq5\":%.10f,\"seq6\":%.10f,\"seq7\":%.10f,\"seq8\":%.10f,\"total\":%.10f}%n",
            features.kwSim, features.idSim, features.opSim, features.seqSim,
            features.lenSim, features.t1, features.t2,
            features.seq2, features.seq3, features.seq4, features.seq5,
            features.seq6, features.seq7, features.seq8,
            total
        );
    }

    private static Features computeFeatures(String code1, String code2) {
        String processed1 = Preprocessor.process(code1);
        String processed2 = Preprocessor.process(code2);

        ArrayList<String> tokens1 = Lexer.tokenize(processed1);
        ArrayList<String> tokens2 = Lexer.tokenize(processed2);

        HashMap<Integer> kwMap1 = KeywordAnalyzer.analyze(tokens1);
        HashMap<Integer> kwMap2 = KeywordAnalyzer.analyze(tokens2);
        FrequencyVector kwVec1 = KeywordAnalyzer.toVector(kwMap1);
        FrequencyVector kwVec2 = KeywordAnalyzer.toVector(kwMap2);
        double kwSim = FrequencyVector.cosineSimilarity(kwVec1, kwVec2);

        HashMap<Integer> opMap1 = OperatorAnalyzer.analyze(tokens1);
        HashMap<Integer> opMap2 = OperatorAnalyzer.analyze(tokens2);
        FrequencyVector opVec1 = OperatorAnalyzer.toVector(opMap1);
        FrequencyVector opVec2 = OperatorAnalyzer.toVector(opMap2);
        double opSim = FrequencyVector.cosineSimilarity(opVec1, opVec2);

        double idSim = identifierSequenceNGramSimilarity(tokens1, tokens2);

        double seq2 = normalizedTokenNGramCosine(tokens1, tokens2, 2);
        double seq3 = normalizedTokenNGramCosine(tokens1, tokens2, 3);
        double seq4 = normalizedTokenNGramCosine(tokens1, tokens2, 4);
        double seq5 = normalizedTokenNGramCosine(tokens1, tokens2, 5);
        double seq6 = normalizedTokenNGramCosine(tokens1, tokens2, 6);
        double seq7 = normalizedTokenNGramCosine(tokens1, tokens2, 7);
        double seq8 = normalizedTokenNGramCosine(tokens1, tokens2, 8);
        double seqSim = 0.05 * seq2 + 0.08 * seq3 + 0.10 * seq4 + 0.12 * seq5 + 0.15 * seq6 + 0.20 * seq7 + 0.30 * seq8;
        double lenSim = tokenLengthSimilarity(tokens1.size(), tokens2.size());

        return new Features(kwSim, idSim, opSim, seqSim, lenSim, tokens1.size(), tokens2.size(), seq2, seq3, seq4, seq5, seq6, seq7, seq8);
    }

    private static double identifierSequenceNGramSimilarity(ArrayList<String> tokens1, ArrayList<String> tokens2) {
        ArrayList<String> ids1 = IdentifierAnalyzer.normalizedIdentifierSequence(tokens1);
        ArrayList<String> ids2 = IdentifierAnalyzer.normalizedIdentifierSequence(tokens2);

        double s2 = cosineSimilaritySparseCounts(NGramAnalyzer.analyzeNGrams(ids1, 2), NGramAnalyzer.analyzeNGrams(ids2, 2));
        double s3 = cosineSimilaritySparseCounts(NGramAnalyzer.analyzeNGrams(ids1, 3), NGramAnalyzer.analyzeNGrams(ids2, 3));
        double s4 = cosineSimilaritySparseCounts(NGramAnalyzer.analyzeNGrams(ids1, 4), NGramAnalyzer.analyzeNGrams(ids2, 4));
        double s5 = cosineSimilaritySparseCounts(NGramAnalyzer.analyzeNGrams(ids1, 5), NGramAnalyzer.analyzeNGrams(ids2, 5));
        double s6 = cosineSimilaritySparseCounts(NGramAnalyzer.analyzeNGrams(ids1, 6), NGramAnalyzer.analyzeNGrams(ids2, 6));

        return 0.10 * s2 + 0.15 * s3 + 0.20 * s4 + 0.25 * s5 + 0.30 * s6;
    }

    private static double normalizedTokenNGramCosine(ArrayList<String> tokens1, ArrayList<String> tokens2, int n) {
        HashMap<Integer> grams1 = NGramAnalyzer.analyzeNormalizedNGrams(tokens1, n);
        HashMap<Integer> grams2 = NGramAnalyzer.analyzeNormalizedNGrams(tokens2, n);
        return cosineSimilaritySparseCounts(grams1, grams2);
    }

    private static double cosineSimilaritySparseCounts(HashMap<Integer> map1, HashMap<Integer> map2) {
        String[] keys1 = map1.keys();
        String[] keys2 = map2.keys();

        if (keys1.length == 0 && keys2.length == 0) {
            return 1.0;
        }
        if (keys1.length == 0 || keys2.length == 0) {
            return 0.0;
        }

        HashMap<Integer> a = map1;
        HashMap<Integer> b = map2;
        String[] aKeys = keys1;
        String[] bKeys = keys2;
        if (keys1.length > keys2.length) {
            a = map2;
            b = map1;
            aKeys = keys2;
            bKeys = keys1;
        }

        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (String k : aKeys) {
            int av = a.getOrDefault(k, 0);
            int bv = b.getOrDefault(k, 0);
            dot += (double) av * bv;
            normA += (double) av * av;
        }

        for (String k : bKeys) {
            int bv = b.getOrDefault(k, 0);
            normB += (double) bv * bv;
        }

        if (normA == 0 && normB == 0) return 1.0;
        if (normA == 0 || normB == 0) return 0.0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private static double tokenLengthSimilarity(int n1, int n2) {
        if (n1 == 0 && n2 == 0) return 1.0;
        if (n1 == 0 || n2 == 0) return 0.0;
        return (double) Math.min(n1, n2) / (double) Math.max(n1, n2);
    }

    private static String readFile(String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    private record Features(double kwSim, double idSim, double opSim, double seqSim, double lenSim, int t1, int t2, double seq2, double seq3, double seq4, double seq5, double seq6, double seq7, double seq8) {}
}
