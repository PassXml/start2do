package org.start2do.util;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.Getter;

public class ChaCha20Poly1305Util {

    @Getter
    private static Encoder encoder = Base64.getEncoder();
    @Getter
    private static Decoder decoder = Base64.getDecoder();


    public static byte[] encrypt(byte[] data, String key, String iv) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("ChaCha20-Poly1305/None/NoPadding");
            AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec keySpec = new SecretKeySpec(Base64.getDecoder().decode(key), "ChaCha20");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException |
                 IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] decrypt(byte[] cipherText, byte[] key, byte[] iv) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("ChaCha20-Poly1305/None/NoPadding");
            AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            SecretKeySpec keySpec = new SecretKeySpec(key, "ChaCha20");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);
            return cipher.doFinal(cipherText);
        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException |
                 NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] decryptBase64Key(byte[] cipherText, String key, String iv) {
        return decrypt(cipherText, decoder.decode(key), iv.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] decryptBase64KeyIv(byte[] cipherText, byte[] key, byte[] iv) {
        return decrypt(cipherText, decoder.decode(key), iv);
    }

    public static byte[] decrypt(byte[] cipherText, String key, String iv) {
        return decrypt(cipherText, decoder.decode(key), iv.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] decrypt(byte[] cipherText, String key, byte[] iv) {
        return decrypt(cipherText, decoder.decode(key), iv);
    }

    public static String generateKey() {
        return encoder.encodeToString(generateKeyByte());
    }

    public static byte[] generateKeyByte() {
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("ChaCha20");
            //Keysize MUST be 256 bit - as of Java11 only 256Bit is supported
            keyGenerator.init(256);
            return keyGenerator.generateKey().getEncoded();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static CipherOutputStream encrypt(OutputStream outputStream, String key, String iv) {
        return encrypt(outputStream, getDecoder().decode(key), iv.getBytes(StandardCharsets.UTF_8));

    }


    public static CipherOutputStream encrypt(OutputStream outputStream, byte[] key, byte[] iv) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("ChaCha20-Poly1305/None/NoPadding");
            AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            SecretKeySpec keySpec = new SecretKeySpec(key, "ChaCha20");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);
            return new CipherOutputStream(outputStream, cipher);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
