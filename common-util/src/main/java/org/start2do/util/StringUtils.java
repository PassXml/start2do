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
}
