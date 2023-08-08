package org.start2do.util;

import java.math.BigInteger;
import lombok.experimental.UtilityClass;

@UtilityClass
public class HexUtil {

    /**
     * 定长数组转化String
     */
    public static String fixedBytesToString(byte[] bytes) {
        int endLength = bytes.length;
        int consecutiveZeroCount = 0;
        for (int i = bytes.length - 1; i >= 0; i--) {
            if (bytes[i] != 0) {
                endLength = i;
                break;
            }
        }
        return new String(bytes, 0, endLength + 1);
    }

    public static int toInt(String number) {
        return new BigInteger(number, 16).intValue();
    }

    public static String toHex(Integer i) {
        return "0x" + Integer.toHexString(i);
    }
}
