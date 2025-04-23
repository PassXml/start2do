package org.start2do.bpm.interfaces;

/**
 * 获取当前用户名称
 */
public interface ITokenUtil {

    default String getCurUserName() {
        return "Unknown";
    }

    default String getCurUserRealName() {
        return "Unknown";
    }

    default String getCurUserId() {
        return "Unknown";
    }

}
