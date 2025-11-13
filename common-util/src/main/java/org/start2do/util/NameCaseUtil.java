package org.start2do.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

/**
 * 名称/命名风格转换工具。
 * <p>
 * 支持在多种命名规则之间进行互转：
 * - lowerCamel（小驼峰，如: dataSource）
 * - UpperCamel（大驼峰/帕斯卡，如: DataSource）
 * - lower_snake（小写下划线，如: data_source）
 * - UPPER_SNAKE（常量/全大写下划线，如: DATA_SOURCE）
 * - lower-kebab（短横线，如: data-source）
 * - UPPER-KEBAB（全大写短横线，如: DATA-SOURCE）
 * - Title Case（单词首字母大写空格分隔，如: Data Source）
 * <p>
 * 典型示例：
 * NameCaseUtil.toUpperSnake("userName") => "USER_NAME"
 * NameCaseUtil.toLowerCamel("USER_NAME") => "userName"
 * NameCaseUtil.toLowerKebab("HTTPServer_v2") => "http-server-v2"
 */
@UtilityClass
public class NameCaseUtil {

    /** 命名风格枚举，用于统一入口 convert 方法。 */
    public enum CaseStyle {
        LOWER_CAMEL, UPPER_CAMEL, LOWER_SNAKE, UPPER_SNAKE, LOWER_KEBAB, UPPER_KEBAB, TITLE
    }

    /**
     * 统一入口：将输入转换为目标命名风格。
     *
     * @param input 原始字符串（允许驼峰/下划线/短横线/点/空格混合）
     * @param target 目标命名风格
     * @return 转换后的字符串；当 input 为 null 时返回 null
     */
    public static String convert(String input, CaseStyle target) {
        if (input == null) {
            return null;
        }
        Objects.requireNonNull(target, "target style must not be null");
        switch (target) {
            case LOWER_CAMEL:
                return toLowerCamel(input);
            case UPPER_CAMEL:
                return toUpperCamel(input);
            case LOWER_SNAKE:
                return toLowerSnake(input);
            case UPPER_SNAKE:
                return toUpperSnake(input);
            case LOWER_KEBAB:
                return toLowerKebab(input);
            case UPPER_KEBAB:
                return toUpperKebab(input);
            case TITLE:
                return toTitle(input);
            default:
                // 理论不会到此
                return input;
        }
    }

    // =============== 具体风格方法（便捷调用） ===============

    public static String toLowerCamel(String input) {
        if (input == null) {
            return null;
        }
        List<String> words = splitToWords(input);
        if (words.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(words.get(0).toLowerCase(Locale.ROOT));
        for (int i = 1; i < words.size(); i++) {
            sb.append(capitalize(words.get(i)));
        }
        return sb.toString();
    }

    public static String toUpperCamel(String input) {
        if (input == null) {
            return null;
        }
        List<String> words = splitToWords(input);
        if (words.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            sb.append(capitalize(w));
        }
        return sb.toString();
    }

    public static String toLowerSnake(String input) {
        return joinWithDelimiter(input, "_", false);
    }

    /** 常量命名：等价于“驼峰转全大写下划线”。 */
    public static String toUpperSnake(String input) {
        return joinWithDelimiter(input, "_", true);
    }

    public static String toLowerKebab(String input) {
        return joinWithDelimiter(input, "-", false);
    }

    public static String toUpperKebab(String input) {
        return joinWithDelimiter(input, "-", true);
    }

    /** 单词首字母大写、以空格分隔（标题风格）。 */
    public static String toTitle(String input) {
        if (input == null) {
            return null;
        }
        List<String> words = splitToWords(input);
        if (words.isEmpty()) {
            return "";
        }
        return words.stream().map(NameCaseUtil::capitalize).collect(Collectors.joining(" "));
    }

    // =============== 内部通用方法 ===============

    private static String joinWithDelimiter(String input, String delimiter, boolean upperCase) {
        if (input == null) {
            return null;
        }
        List<String> words = splitToWords(input);
        if (words.isEmpty()) {
            return "";
        }
        if (upperCase) {
            return words.stream().map(w -> w.toUpperCase(Locale.ROOT)).collect(Collectors.joining(delimiter));
        }
        return words.stream().map(w -> w.toLowerCase(Locale.ROOT)).collect(Collectors.joining(delimiter));
    }

    /**
     * 将输入拆分为语义单词列表。
     * 支持以下边界/分隔符：
     * - 大小写边界（aA / 9A / HTTPServer -> HTTP Server）
     * - 下划线/短横线/点/空白分隔
     * - 连续分隔符合并
     */
    public static List<String> splitToWords(String input) {
        if (input == null) {
            return Collections.emptyList();
        }
        String s = input.trim();
        if (s.isEmpty()) {
            return Collections.emptyList();
        }

        // 处理驼峰与缩写边界："HTTPServer" -> "HTTP Server"，"userID" -> "user ID"
        s = s.replaceAll("([A-Z]+)([A-Z][a-z])", "$1 $2");
        // 处理小写/数字到大写的边界："userName1A" -> "user Name1 A"
        s = s.replaceAll("([a-z\\u00DF-\\u024F\\d])([A-Z])", "$1 $2");

        // 统一其他分隔符为单空格（下划线、短横线、点、空白）
        s = s.replaceAll("[\\s_\\-\\.]+", " ");

        // 拆分并过滤空项
        String[] parts = s.split(" ");
        List<String> words = new ArrayList<>(parts.length);
        for (String p : parts) {
            if (p == null || p.isEmpty()) {
                continue;
            }
            words.add(p);
        }
        return words;
    }

    private static String capitalize(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        if (word.length() == 1) {
            return word.toUpperCase(Locale.ROOT);
        }
        char first = word.charAt(0);
        String rest = word.substring(1);
        return Character.toString(Character.toUpperCase(first)) + rest.toLowerCase(Locale.ROOT);
    }
}

