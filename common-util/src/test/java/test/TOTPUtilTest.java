package test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.start2do.util.TOTPUtil;

public class TOTPUtilTest {

    private static String key;

    private static String secret;

    @BeforeAll
    public static void initKey() {
        TOTPUtilTest.key = TOTPUtil.generateSecretKey();
        System.out.println("生成Key为:" + key);
        TOTPUtilTest.secret = TOTPUtil.getCurrentTOTP(key);
        System.out.println("生成一次性密码为:" + secret);
    }

    @Test
    public void Test1() {
        Assertions.assertTrue(TOTPUtil.verifyTOTP(key, secret));
    }


}
