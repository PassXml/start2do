package org.start2do.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    /**
     * 渲染模板字符串，使用默认占位符格式 {{key}} 将模板中的占位符替换为对应的实际值
     *
     * @param template     模板字符串，包含占位符的原始文本
     * @param placeholders 占位符键值对映射，key为占位符名称，value为替换值
     * @return 渲染后的字符串，所有占位符都被替换为实际值
     * <p>
     * 使用示例: Map<String, String> data = new HashMap<>(); data.put("name", "张三"); data.put("age", "25"); String result =
     * renderTemplate("你好，{{name}}！你今年{{age}}岁了。", data); // 结果: "你好，张三！你今年25岁了。"
     */
    public static String renderTemplate(String template, Map<String, String> placeholders) {
        return renderTemplate(template, "{{", "}}", placeholders);
    }
    /**
     * 渲染模板字符串，支持自定义占位符分隔符
     * 使用KMP算法高效查找和替换模板中的占位符
     *
     * @param template 模板字符串，包含占位符的原始文本
     * @param delimiterStartChar 占位符开始分隔符，如 "{{" 或 "${"
     * @param delimiterendChar 占位符结束分隔符，如 "}}" 或 "}"
     * @param placeholders 占位符键值对映射，key为占位符名称，value为替换值
     * @return 渲染后的字符串，所有匹配的占位符都被替换为实际值
     *
     * 实现说明:
     * 1. 遍历所有占位符键值对
     * 2. 为每个键构造完整的占位符格式 (分隔符+键+分隔符)
     * 3. 使用KMP算法查找占位符在模板中的位置
     * 4. 将找到的占位符替换为对应的值
     * 5. 通过offset变量处理替换后字符串长度变化的影响
     * 6. 重复查找直到所有相同占位符都被替换
     *
     * 使用示例:
     * String result = renderTemplate("欢迎 ${name}，年龄：${age}", "${", "}", data);
     * // 结果: "欢迎 张三，年龄：25"
     */
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

    /**
     * 从字符串中提取指定方法名的参数
     * 例如: extractParams("userId(\"1\")", "userId") 返回 ["1"]
     * 例如: extractParams("userId(\"1\",\"2\")", "userId") 返回 ["1", "2"]
     *
     * @param input 输入字符串
     * @param methodName 方法名
     * @return 参数数组，如果未找到则返回空数组
     */
    public static String[] extractParams(String input, String methodName) {
        if (isEmpty(input) || isEmpty(methodName)) {
            return new String[0];
        }

        // 构建匹配模式：methodName(...)
        String prefix = methodName + "(";
        String suffix = ")";

        // 使用现有的 extractString 方法提取括号内的内容
        String paramsContent = extractString(input, prefix, suffix);
        if (paramsContent == null) {
            return new String[0];
        }

        // 解析参数内容
        return parseParameters(paramsContent);
    }

    /**
     * 解析参数字符串，支持带引号的参数 例如: "\"1\",\"2\"" 返回 ["1", "2"] 例如: "\"hello\",\"world\"" 返回 ["hello", "world"]
     *
     * @param paramsContent 参数内容字符串
     * @return 解析后的参数数组
     */
    private static String[] parseParameters(String paramsContent) {
        if (isEmpty(paramsContent)) {
            return new String[0];
        }

        java.util.List<String> params = new java.util.ArrayList<>();
        StringBuilder currentParam = new StringBuilder();
        boolean inQuotes = false;
        boolean escapeNext = false;

        for (int i = 0; i < paramsContent.length(); i++) {
            char c = paramsContent.charAt(i);

            if (escapeNext) {
                currentParam.append(c);
                escapeNext = false;
                continue;
            }

            if (c == '\\') {
                escapeNext = true;
                continue;
            }

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                // 遇到逗号且不在引号内，表示参数分隔
                String param = currentParam.toString().trim();
                if (!param.isEmpty()) {
                    params.add(param);
                }
                currentParam.setLength(0);
            } else if (!Character.isWhitespace(c) || inQuotes) {
                // 非空白字符或在引号内的空白字符都保留
                currentParam.append(c);
            }
        }

        // 添加最后一个参数
        String lastParam = currentParam.toString().trim();
        if (!lastParam.isEmpty()) {
            params.add(lastParam);
        }

        return params.toArray(new String[0]);
    }

}
