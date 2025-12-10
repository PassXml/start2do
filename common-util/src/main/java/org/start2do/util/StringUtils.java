package org.start2do.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    public static String md5(String input) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return "check jdk";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        char[] charArray = input.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++) {
            byteArray[i] = (byte) charArray[i];
        }
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    /**
     * 驼峰命名转为大写下划线命名（UPPER_SNAKE_CASE）
     *
     * 例如：userName -> USER_NAME ，UserName -> USER_NAME
     */
    public String camelToUpperSnake(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        StringBuilder sb = new StringBuilder();
        char[] chars = input.trim().toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (Character.isUpperCase(c)) {
                // 当前是大写字母并且前面不是开头、下划线或大写连续块的中间，则补一个下划线
                if (i > 0) {
                    char prev = chars[i - 1];
                    boolean prevIsUpper = Character.isUpperCase(prev);
                    boolean prevIsUnderscore = prev == '_';
                    // 如果上一个是小写或数字，或者上一个是大写但后一个是小写，则认为是新单词边界
                    boolean needUnderscore = !prevIsUpper && !prevIsUnderscore;
                    if (!needUnderscore && prevIsUpper && i + 1 < chars.length) {
                        char next = chars[i + 1];
                        if (Character.isLowerCase(next)) {
                            needUnderscore = true;
                        }
                    }
                    if (needUnderscore && sb.charAt(sb.length() - 1) != '_') {
                        sb.append('_');
                    }
                }
                sb.append(Character.toUpperCase(c));
            } else {
                sb.append(Character.toUpperCase(c));
            }
        }
        return sb.toString();
    }

    /**
     * 大写下划线命名（UPPER_SNAKE_CASE）转为小驼峰命名（lowerCamelCase）
     *
     * 例如：USER_NAME -> userName
     */
    public String upperSnakeToLowerCamel(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        StringBuilder sb = new StringBuilder();
        String[] parts = input.toLowerCase().split("_");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) {
                continue;
            }
            if (i == 0) {
                sb.append(part);
            } else {
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    sb.append(part.substring(1));
                }
            }
        }
        return sb.toString();
    }

    /**
     * 大写下划线命名（UPPER_SNAKE_CASE）转为大驼峰命名（UpperCamelCase）
     *
     * 例如：USER_NAME -> UserName
     */
    public String upperSnakeToUpperCamel(String input) {
        String lowerCamel = upperSnakeToLowerCamel(input);
        if (lowerCamel == null || lowerCamel.isEmpty()) {
            return lowerCamel;
        }
        return Character.toUpperCase(lowerCamel.charAt(0)) + lowerCamel.substring(1);
    }
}
