package org.start2do.constant;

public interface ErrorConstant {

    /**
     *  未登录
     */
    String NOT_LOGIN = "401-0";
    //    认证失败或者凭证过期
    String AUTHENTICATION_FAILED_OR_EXPIRED = "401-2";
    /**
     *  权限不足
     */
    String PERMISSION_DENIED = "401-1";
    /**
      *  无权限
     */
    String NO_PERMISSION = "401-3";
}
