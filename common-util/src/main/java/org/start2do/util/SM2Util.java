package org.start2do.util;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.engines.SM2Engine.Mode;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECKeyParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.math.ec.ECConstants;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.BigIntegers;
import org.bouncycastle.util.encoders.Hex;

@Slf4j
public class SM2Util {

    private final Digest digest;

    /**
     * 是否为加密模式
     */
    private boolean forEncryption;
    private ECKeyParameters ecKey;
    private ECDomainParameters ecParams;
    private int curveLength;
    private SecureRandom random;
    /**
     * 密文排序方式
     */
    @Setter
    private Mode cipherMode;


    public SM2Util() {
        this.digest = new SM3Digest();
    }

    public SM2Util(Digest digest) {
        this.digest = digest;
    }


    /**
     * 默认初始化方法，使用国密排序标准
     *
     * @param forEncryption - 是否以加密模式初始化
     * @param param         - 曲线参数
     */
    public void init(boolean forEncryption, CipherParameters param) {
        init(forEncryption, Mode.C1C3C2, param);
    }

    /**
     * 默认初始化方法，使用国密排序标准
     *
     * @param forEncryption 是否以加密模式初始化
     * @param cipherMode    加密数据排列模式：1-标准排序；0-BC默认排序
     * @param param         曲线参数
     */
    public void init(boolean forEncryption, Mode cipherMode, CipherParameters param) {
        this.forEncryption = forEncryption;
        this.cipherMode = cipherMode;
        if (forEncryption) {
            ParametersWithRandom rParam = (ParametersWithRandom) param;

            ecKey = (ECKeyParameters) rParam.getParameters();
            ecParams = ecKey.getParameters();

            ECPoint s = ((ECPublicKeyParameters) ecKey).getQ().multiply(ecParams.getH());
            if (s.isInfinity()) {
                throw new IllegalArgumentException("invalid key: [h]Q at infinity");
            }

            random = rParam.getRandom();
        } else {
            ecKey = (ECKeyParameters) param;
            ecParams = ecKey.getParameters();
        }

        curveLength = (ecParams.getCurve().getFieldSize() + 7) / 8;
    }

    /**
     * 加密或解密输入数据
     *
     * @param in
     * @param inOff
     * @param inLen
     * @return
     * @throws InvalidCipherTextException
     */
    public byte[] processBlock(byte[] in, int inOff, int inLen) throws InvalidCipherTextException {
        if (forEncryption) {
            // 加密
            return encrypt(in, inOff, inLen);
        } else {
            return decrypt(in, inOff, inLen);
        }
    }

    /**
     * 加密实现，根据cipherMode输出指定排列的结果，默认按标准方式排列
     *
     * @param in
     * @param inOff
     * @param inLen
     * @return
     * @throws InvalidCipherTextException
     */
    private byte[] encrypt(byte[] in, int inOff, int inLen)
        throws InvalidCipherTextException {
        byte[] c2 = new byte[inLen];

        System.arraycopy(in, inOff, c2, 0, c2.length);

        byte[] c1;
        ECPoint kPB;
        do {
            BigInteger k = nextK();

            ECPoint c1P = ecParams.getG().multiply(k).normalize();

            c1 = c1P.getEncoded(false);

            kPB = ((ECPublicKeyParameters) ecKey).getQ().multiply(k).normalize();

            kdf(digest, kPB, c2);
        }
        while (notEncrypted(c2, in, inOff));

        byte[] c3 = new byte[digest.getDigestSize()];

        addFieldElement(digest, kPB.getAffineXCoord());
        digest.update(in, inOff, inLen);
        addFieldElement(digest, kPB.getAffineYCoord());

        digest.doFinal(c3, 0);
        if (cipherMode == Mode.C1C3C2) {
            return Arrays.concatenate(c1, c3, c2);
        }
        return Arrays.concatenate(c1, c2, c3);
    }

    /**
     * 解密实现，默认按标准排列方式解密，解密时解出c2部分原文并校验c3部分
     *
     * @param in
     * @param inOff
     * @param inLen
     * @return
     * @throws InvalidCipherTextException
     */
    private byte[] decrypt(byte[] in, int inOff, int inLen)
        throws InvalidCipherTextException {
        byte[] c1 = new byte[curveLength * 2 + 1];

        System.arraycopy(in, inOff, c1, 0, c1.length);

        ECPoint c1P = ecParams.getCurve().decodePoint(c1);

        ECPoint s = c1P.multiply(ecParams.getH());
        if (s.isInfinity()) {
            throw new InvalidCipherTextException("[h]C1 at infinity");
        }

        c1P = c1P.multiply(((ECPrivateKeyParameters) ecKey).getD()).normalize();

        byte[] c2 = new byte[inLen - c1.length - digest.getDigestSize()];
        if (cipherMode == Mode.C1C2C3) {
            System.arraycopy(in, inOff + c1.length, c2, 0, c2.length);
        } else {
            // C1 C3 C2
            System.arraycopy(in, inOff + c1.length + digest.getDigestSize(), c2, 0, c2.length);
        }

        kdf(digest, c1P, c2);

        byte[] c3 = new byte[digest.getDigestSize()];

        addFieldElement(digest, c1P.getAffineXCoord());
        digest.update(c2, 0, c2.length);
        addFieldElement(digest, c1P.getAffineYCoord());

        digest.doFinal(c3, 0);

        int check = 0;
        // 检查密文输入值C3部分和由摘要生成的C3是否一致
        if (cipherMode == Mode.C1C2C3) {
            for (int i = 0; i != c3.length; i++) {
                check |= c3[i] ^ in[c1.length + c2.length + i];
            }
        } else {
            for (int i = 0; i != c3.length; i++) {
                check |= c3[i] ^ in[c1.length + i];
            }
        }

        clearBlock(c1);
        clearBlock(c3);

        if (check != 0) {
            clearBlock(c2);
            throw new InvalidCipherTextException("invalid cipher text");
        }

        return c2;
    }

    private boolean notEncrypted(byte[] encData, byte[] in, int inOff) {
        for (int i = 0; i != encData.length; i++) {
            if (encData[i] != in[inOff]) {
                return false;
            }
        }

        return true;
    }

    private void kdf(Digest digest, ECPoint c1, byte[] encData) {
        int ct = 1;
        int v = digest.getDigestSize();

        byte[] buf = new byte[digest.getDigestSize()];
        int off = 0;

        for (int i = 1; i <= ((encData.length + v - 1) / v); i++) {
            addFieldElement(digest, c1.getAffineXCoord());
            addFieldElement(digest, c1.getAffineYCoord());
            digest.update((byte) (ct >> 24));
            digest.update((byte) (ct >> 16));
            digest.update((byte) (ct >> 8));
            digest.update((byte) ct);

            digest.doFinal(buf, 0);

            if (off + buf.length < encData.length) {
                xor(encData, buf, off, buf.length);
            } else {
                xor(encData, buf, off, encData.length - off);
            }

            off += buf.length;
            ct++;
        }
    }

    private void xor(byte[] data, byte[] kdfOut, int dOff, int dRemaining) {
        for (int i = 0; i != dRemaining; i++) {
            data[dOff + i] ^= kdfOut[i];
        }
    }

    private BigInteger nextK() {
        int qBitLength = ecParams.getN().bitLength();

        BigInteger k;
        do {
            k = new BigInteger(qBitLength, random);
        }
        while (k.equals(ECConstants.ZERO) || k.compareTo(ecParams.getN()) >= 0);

        return k;
    }

    private void addFieldElement(Digest digest, ECFieldElement v) {
        byte[] p = BigIntegers.asUnsignedByteArray(curveLength, v.toBigInteger());

        digest.update(p, 0, p.length);
    }

    /**
     * clear possible sensitive data
     */
    private void clearBlock(
        byte[] block) {
        for (int i = 0; i != block.length; i++) {
            block[i] = 0;
        }
    }

    /**
     * 获取sm2密钥对 BC库使用的公钥=64个字节+1个字节（04标志位），BC库使用的私钥=32个字节 SM2秘钥的组成部分有 私钥D 、公钥X 、 公钥Y , 他们都可以用长度为64的16进制的HEX串表示，
     * <br/>SM2公钥并不是直接由X+Y表示 , 而是额外添加了一个头，当启用压缩时:公钥=有头+公钥X ，即省略了公钥Y的部分
     *
     * @param compressed 是否压缩公钥（加密解密都使用BC库才能使用压缩）
     * @return
     */
    public static KeyPairOfString getSm2Keys(boolean compressed) {
        //获取一条SM2曲线参数
        X9ECParameters sm2ECParameters = GMNamedCurves.getByName("sm2p256v1");
        //构造domain参数
        ECDomainParameters domainParameters = new ECDomainParameters(sm2ECParameters.getCurve(), sm2ECParameters.getG(),
            sm2ECParameters.getN());
        //1.创建密钥生成器
        ECKeyPairGenerator keyPairGenerator = new ECKeyPairGenerator();
        //2.初始化生成器,带上随机数
        try {
            keyPairGenerator.init(
                new ECKeyGenerationParameters(domainParameters, SecureRandom.getInstance("SHA1PRNG")));
        } catch (NoSuchAlgorithmException e) {
            log.error("生成公私钥对时出现异常:", e);
        }
        //3.生成密钥对
        AsymmetricCipherKeyPair asymmetricCipherKeyPair = keyPairGenerator.generateKeyPair();
        ECPublicKeyParameters publicKeyParameters = (ECPublicKeyParameters) asymmetricCipherKeyPair.getPublic();
        ECPoint ecPoint = publicKeyParameters.getQ();
        // 把公钥放入map中,默认压缩公钥
        // 公钥前面的02或者03表示是压缩公钥,04表示未压缩公钥,04的时候,可以去掉前面的04
        String publicKey = Hex.toHexString(ecPoint.getEncoded(compressed));
        ECPrivateKeyParameters privateKeyParameters = (ECPrivateKeyParameters) asymmetricCipherKeyPair.getPrivate();
        BigInteger intPrivateKey = privateKeyParameters.getD();
        // 把私钥放入map中
        String privateKey = intPrivateKey.toString(16);
        return new KeyPairOfString(publicKey, privateKey);
    }

    /**
     * SM2加密算法
     *
     * @param publicKey 公钥
     * @param data      待加密的数据
     * @return 密文，BC库产生的密文带由04标识符，与非BC库对接时需要去掉开头的04
     */
    public static String encrypt(String publicKey, String data) {
        // 按国密排序标准加密
        return encrypt(publicKey, data, Mode.C1C3C2);
    }

    /**
     * SM2加密算法
     *
     * @param publicKey  公钥
     * @param data       待加密的数据
     * @param cipherMode 密文排列方式0-C1C2C3；1-C1C3C2；
     * @return 密文，BC库产生的密文带由04标识符，与非BC库对接时需要去掉开头的04
     */
    public static String encrypt(String publicKey, String data, Mode cipherMode) {
        // 获取一条SM2曲线参数
        X9ECParameters sm2ECParameters = GMNamedCurves.getByName("sm2p256v1");
        // 构造ECC算法参数，曲线方程、椭圆曲线G点、大整数N
        ECDomainParameters domainParameters = new ECDomainParameters(sm2ECParameters.getCurve(), sm2ECParameters.getG(),
            sm2ECParameters.getN());
        //提取公钥点
        ECPoint pukPoint = sm2ECParameters.getCurve().decodePoint(Hex.decode(publicKey));
        // 公钥前面的02或者03表示是压缩公钥，04表示未压缩公钥, 04的时候，可以去掉前面的04
        ECPublicKeyParameters publicKeyParameters = new ECPublicKeyParameters(pukPoint, domainParameters);

        SM2Util sm2Engine = new SM2Util();
        // 设置sm2为加密模式
        sm2Engine.init(true, cipherMode, new ParametersWithRandom(publicKeyParameters, new SecureRandom()));

        byte[] arrayOfBytes = null;
        try {
            byte[] in = data.getBytes();
            arrayOfBytes = sm2Engine.processBlock(in, 0, in.length);
        } catch (Exception e) {
            log.error("SM2加密时出现异常:{}", e.getMessage(), e);
        }
        return Hex.toHexString(arrayOfBytes);
    }

    /**
     * SM2解密算法
     *
     * @param privateKey 私钥
     * @param cipherData 密文数据
     * @return
     */
    public static String decrypt(String privateKey, String cipherData) {
        // // 按国密排序标准解密
        return decrypt(privateKey, cipherData, Mode.C1C3C2);
    }

    /**
     * SM2解密算法
     *
     * @param privateKey 私钥
     * @param cipherData 密文数据
     * @param cipherMode 密文排列方式0-C1C2C3；1-C1C3C2；
     * @return
     */
    public static String decrypt(String privateKey, String cipherData, Mode cipherMode) {
        // 使用BC库加解密时密文以04开头，传入的密文前面没有04则补上
        if (!cipherData.startsWith("04")) {
            cipherData = "04" + cipherData;
        }
        byte[] cipherDataByte = Hex.decode(cipherData);

        //获取一条SM2曲线参数
        X9ECParameters sm2ECParameters = GMNamedCurves.getByName("sm2p256v1");
        //构造domain参数
        ECDomainParameters domainParameters = new ECDomainParameters(sm2ECParameters.getCurve(), sm2ECParameters.getG(),
            sm2ECParameters.getN());

        BigInteger privateKeyD = new BigInteger(privateKey, 16);
        ECPrivateKeyParameters privateKeyParameters = new ECPrivateKeyParameters(privateKeyD, domainParameters);

        SM2Util sm2Engine = new SM2Util();
        // 设置sm2为解密模式
        sm2Engine.init(false, cipherMode, privateKeyParameters);

        String result = "";
        try {
            byte[] arrayOfBytes = sm2Engine.processBlock(cipherDataByte, 0, cipherDataByte.length);
            return new String(arrayOfBytes);
        } catch (Exception e) {
            log.error("SM2解密时出现异常:{}", e.getMessage(), e);
        }
        return result;

    }

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeyPairOfString {

        private String publicKey;
        private String privateKey;
    }

    public static void main(String[] args) {
        KeyPairOfString keys = SM2Util.getSm2Keys(true);
        String privateKey = keys.getPrivateKey();
        String publicKey = keys.getPublicKey();
        //给前端的时候..有个04..前端可能会报错处理不了
        System.out.println(SM2Util.encrypt(publicKey, "ABCD"));
        System.out.println(SM2Util.decrypt(privateKey,
            "cfa0d07f34eecca53c1e8d058d8a9c3cb92041f974244d51e7a47e15802c568b78ca8b663b2c6d8c3d447babc8e347f0a59a7de723c9552ebec5b26e6f40641ba40e7ba13e8b533c5be93351653fafbc9063179d9c369f1139086445ca15c92fba77c705"));
    }
}
