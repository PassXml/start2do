package org.start2do.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TOTPUtil {

    private static final int DEFAULT_PERIOD = 30;
    // 允许前后1个时间窗口的误差
    private static final int DEFAULT_WINDOW_SIZE = 1;
    private static final String HMAC_ALGORITHM = "HmacSHA1";
    private static final int DEFAULT_CODE_DIGITS = 6;

    /**
     * 验证TOTP代码是否有效
     *
     * @param secretKey 密钥
     * @param inputCode 用户输入的代码
     * @return 验证是否通过
     */
    public static boolean verifyTOTP(String secretKey, String inputCode) {
        return verifyTOTP(secretKey, inputCode, DEFAULT_WINDOW_SIZE);
    }

    /**
     * 验证TOTP代码是否有效，可指定时间窗口大小
     *
     * @param secretKey  密钥
     * @param inputCode  用户输入的代码
     * @param windowSize 时间窗口大小（前后各多少个时间周期）
     * @return 验证是否通过
     */
    public static boolean verifyTOTP(String secretKey, String inputCode, int windowSize) {
        long currentTimeCounter = System.currentTimeMillis() / TimeUnit.SECONDS.toMillis(DEFAULT_PERIOD);

        // 检查当前时间窗口及前后的时间窗口
        for (int i = -windowSize; i <= windowSize; i++) {
            String generatedCode = generateTOTP(secretKey, currentTimeCounter + i);
            if (generatedCode.equals(inputCode)) {
                return true;
            }
        }

        return false;
    }


    // 生成随机密钥
    public static String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20]; // 160 bits
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    // 生成 TOTP 代码
    public static String generateTOTP(String secretKey, long timeCounter) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(secretKey);
            byte[] timeBytes = longToBytes(timeCounter);

            Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, HMAC_ALGORITHM);
            hmac.init(keySpec);

            byte[] hash = hmac.doFinal(timeBytes);

            // 动态截断
            int offset = hash[hash.length - 1] & 0xF;
            int binary = ((hash[offset] & 0x7F) << 24) |
                         ((hash[offset + 1] & 0xFF) << 16) |
                         ((hash[offset + 2] & 0xFF) << 8) |
                         (hash[offset + 3] & 0xFF);

            // 生成指定位数的代码
            int code = binary % (int) Math.pow(10, DEFAULT_CODE_DIGITS);
            return String.format("%0" + DEFAULT_CODE_DIGITS + "d", code);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("生成TOTP失败", e);
        }
    }

    // 获取当前的TOTP代码
    public static String getCurrentTOTP(String secretKey) {
        long timeCounter = System.currentTimeMillis() / TimeUnit.SECONDS.toMillis(DEFAULT_PERIOD);
        return generateTOTP(secretKey, timeCounter);
    }

    // 将长整型转换为字节数组
    private static byte[] longToBytes(long time) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (time & 0xFF);
            time >>= 8;
        }
        return result;
    }

    // 生成用于注册到验证器应用的URI
    public static String generateOtpAuthUri(String issuer, String accountName, String secretKey) {
        String encodedIssuer = urlEncode(issuer);
        String encodedAccountName = urlEncode(accountName);
        String base32Secret = base32Encode(Base64.getDecoder().decode(secretKey));

        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=%d&period=%d",
            encodedIssuer, encodedAccountName, base32Secret, encodedIssuer,
            DEFAULT_CODE_DIGITS, DEFAULT_PERIOD);
    }

    // URL编码
    private static String urlEncode(String input) {
        try {
            return java.net.URLEncoder.encode(input, "UTF-8").replace("+", "%20");
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("URL编码失败", e);
        }
    }

    // Base32编码 (简化版)
    private static String base32Encode(byte[] data) {
        final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        StringBuilder result = new StringBuilder();
        int bits = 0;
        int value = 0;

        for (byte b : data) {
            value = (value << 8) | (b & 0xFF);
            bits += 8;
            while (bits >= 5) {
                result.append(BASE32_CHARS.charAt((value >> (bits - 5)) & 0x1F));
                bits -= 5;
            }
        }
        if (bits > 0) {
            result.append(BASE32_CHARS.charAt((value << (5 - bits)) & 0x1F));
        }
        return result.toString();
    }
}
