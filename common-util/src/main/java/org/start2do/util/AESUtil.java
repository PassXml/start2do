package org.start2do.util;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16;

    /**
     * 加密数据
     *
     * @param data 待加密的数据
     * @param key  密钥（必须为16字节）
     * @return Base64编码的加密字符串
     * @throws Exception 加密异常
     */
    public static String encrypt(String data, String key) throws Exception {
        if (key == null || key.length() != 16) {
            throw new IllegalArgumentException("密钥长度必须为16字节");
        }

        byte[] iv = generateIV();
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), ALGORITHM);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] encrypted = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(concatenateArrays(iv, encrypted));
    }

    /**
     * 解密数据
     *
     * @param encryptedData Base64编码的加密字符串
     * @param key           密钥（必须为16字节）
     * @return 原始字符串
     * @throws Exception 解密异常
     */
    public static String decrypt(String encryptedData, String key) throws Exception {
        if (key == null || key.length() != 16) {
            throw new IllegalArgumentException("密钥长度必须为16字节");
        }

        byte[] decodedData = Base64.getDecoder().decode(encryptedData);
        byte[] iv = new byte[IV_LENGTH];
        byte[] encrypted = new byte[decodedData.length - IV_LENGTH];

        System.arraycopy(decodedData, 0, iv, 0, IV_LENGTH);
        System.arraycopy(decodedData, IV_LENGTH, encrypted, 0, encrypted.length);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), ALGORITHM);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted);
    }

    /**
     * 生成随机初始化向量(IV)
     *
     * @return 长度为16的随机字节数组
     */
    private static byte[] generateIV() {
        byte[] iv = new byte[IV_LENGTH];
        new java.security.SecureRandom().nextBytes(iv);
        return iv;
    }

    /**
     * 拼接两个字节数组
     *
     * @param array1 第一个数组
     * @param array2 第二个数组
     * @return 拼接后的数组
     */
    private static byte[] concatenateArrays(byte[] array1, byte[] array2) {
        byte[] result = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }
}
