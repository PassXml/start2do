package org.start2do.bpm.service;

import java.util.List;

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
     * 部门Ids
     */
    List<String> getDeptCodes();

    /**
     * 岗位Id
     */
    List<String> getPositionCodes();

    /**
     * 获取当前用户Id
     */
    String getCurrentUserId();

    String getUserHandler(String username);
}
