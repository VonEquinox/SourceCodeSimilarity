# Java 源代码相似度检测系统

基于关键字/运算符频度与标识符集合的 Java 源代码相似度检测（FinalVer），包含测试代码集（TestCode）。

## 功能概述

1. **预处理**：去除注释与字符串/字符字面量
2. **词法分析**：提取关键字、运算符、标识符
3. **相似度计算**：
   - 关键字：余弦相似度
   - 运算符：余弦相似度
   - 标识符：Jaccard 系数
   - 权重：关键字 0.35 + 标识符 0.40 + 运算符 0.25

说明：数字字面量不会进入标识符统计，`true/false/null` 也会被排除。

## 快速开始

```bash
cd FinalVer
javac Main.java Analyzer/*.java DataStructure/*.java Constants/*.java
java Main Test1.java Test2.java
```

使用测试集示例：

```bash
java Main ../TestCode/LinkedList/LinkedList1.java ../TestCode/LinkedList/LinkedList2.java
```

## 项目结构

```
SourceCodeSimilarity/
│
├── FinalVer/                              # 最终版（命令行）
│   ├── Analyzer/                          # 预处理与词法分析
│   ├── Constants/                         # Java 关键字/运算符常量
│   ├── DataStructure/                     # 自定义数据结构
│   ├── Main.java                          # 入口
│   └── SimilarityCalculator.java          # 相似度计算核心
│
├── TestCode/                              # 测试代码集
│   ├── LinkedList/
│   ├── Stack/
│   ├── Heap/
│   ├── HashTable/
│   └── SegmentTree/
│
└── README.md
```

## 测试代码集 (`TestCode/`)

每个数据结构包含 4 个实现版本，用于验证相似度检测效果：

| 版本 | 说明 | 预期相似度 |
|------|------|-----------|
| 实现1 | 基本实现 | 基准 |
| 实现2 | 仅修改变量名/函数名 | 高 |
| 实现3 | 不同写法，功能相同 | 中 |
| 实现4 | 功能变体，写法类似1 | 较高 |
