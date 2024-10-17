package org.start2do.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

/**
 * @Author Lijie
 * @date 2021/12/28:14:56
 */
@lombok.Getter
@lombok.Setter
@UtilityClass
public final class StringUtils {

    private Pattern emoji = Pattern.compile(
        "[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
        Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);

    public boolean isBlank(String string) {
        if (isEmpty(string)) {
            return true;
        } else {
            for (int i = 0; i < string.length(); ++i) {
                if (!Character.isWhitespace(string.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
    }

    public boolean isNotBlank(String string) {
        return !isBlank(string);
    }

    public boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public boolean isNotEmpty(String string) {
        return !isEmpty(string);
    }

    public String truncate(String string, int maxLength) {
        return string.length() > maxLength ? string.substring(0, maxLength) : string;
    }

    public boolean hasEmoji(String string) {
        Matcher matcher = emoji.matcher(string);
        return matcher.find();
    }

    public String filterEmoji(String source) {
        if (source != null) {
            Matcher emojiMatcher = emoji.matcher(source);
            if (emojiMatcher.find()) {
                source = emojiMatcher.replaceAll("*");
                return source;
            }
            return source;
        }
        return source;
    }

    public String randomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    public static Map<String, String> getQueryMap(String url) {
        Map<String, String> map = new HashMap<>();
        try {
            String[] parts = url.split("\\?");
            if (parts.length > 1) {
                String query = parts[1];
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    String key = URLDecoder.decode(keyValue[0], "UTF-8");
                    String value = URLDecoder.decode(keyValue[1], "UTF-8");
                    map.put(key, value);
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static String extractString(String input, String prefix, String suffix) {
        // 使用 KMP 算法查找 prefix 的位置
        int startIndex = kmpSearch(input, prefix);
        if (startIndex == -1) {
            return null; // 未找到 prefix
        }

        // 定位到 prefix 之后的位置
        startIndex += prefix.length();

        // 查找 suffix 的位置
        int endIndex = input.indexOf(suffix, startIndex);
        if (endIndex == -1) {
            // 如果没有找到 suffix，则返回从 prefix 之后的所有字符
            return input.substring(startIndex);
        } else {
            // 否则返回 prefix 和 suffix 之间的子字符串
            return input.substring(startIndex, endIndex);
        }
    }

    // KMP 算法查找子字符串的位置
    public static int kmpSearch(String text, String pattern) {
        int[] lps = buildLPS(pattern);
        int i = 0, j = 0;
        while (i < text.length()) {
            if (pattern.charAt(j) == text.charAt(i)) {
                i++;
                j++;
                if (j == pattern.length()) {
                    return i - j; // 找到匹配位置
                }
            } else if (j > 0) {
                j = lps[j - 1];
            } else {
                i++;
            }
        }
        return -1; // 未找到匹配位置
    }

    // 构建部分匹配表（LPS 数组） （KMP算法需要
    private static int[] buildLPS(String pattern) {
        int[] lps = new int[pattern.length()];
        int length = 0;
        int i = 1;
        while (i < pattern.length()) {
            if (pattern.charAt(i) == pattern.charAt(length)) {
                length++;
                lps[i] = length;
                i++;
            } else if (length > 0) {
                length = lps[length - 1];
            } else {
                lps[i] = 0;
                i++;
            }
        }
        return lps;
    }

    /**
     * 判断字符串是否为空
     */
    public static String isNotBlank(String string, String defaultString) {
        if (isNotBlank(string)) {
            return string;
        }
        return defaultString;
    }

    /**
     * 判断字符串是否为空
     */
    public static String isNotEmpty(String string, String defaultString) {
        if (isNotEmpty(string)) {
            return string;
        }
        return defaultString;
    }

    public static String renderTemplate(String template, Map<String, String> placeholders) {
        return renderTemplate(template, "{{", "}}", placeholders);
    }

    public static String renderTemplate(String template, String delimiterStartChar, String delimiterendChar,
        Map<String, String> placeholders) {
        StringBuilder result = new StringBuilder(template);
        int offset = 0;

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String placeholder = delimiterStartChar + entry.getKey() + delimiterendChar;
            String value = entry.getValue();

            int index;
            while ((index = kmpSearch(result.toString(), placeholder)) != -1) {
                result.replace(index + offset, index + offset + placeholder.length(), value);
                offset += value.length() - placeholder.length();
            }
        }

        return result.toString();
    }
}
