package org.start2do.bpm.service;

public interface IUserHandle {

    /**
     * 获取当前用户
     */
    String getCurrentUsername();

    /**
     * 获取当前用户真名
     */
    String getCurrentUserRealName();

    /**
     * 获取当前用户Id
     */
    String getCurrentUserId();

    String getUserHandler(String username);
}
