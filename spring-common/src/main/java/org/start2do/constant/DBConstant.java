package org.start2do.constant;

/**
 * 数据库常用字段长度
 */
public interface DBConstant {

    /**
     * ID长度
     */
    int ID_STR_LENGHT = 64;
    /**
     * 标题
     */
    int TITLE_LENGHT = 128;
    /**
     * URL
     */
    int URL_LENGHT = 4096;

    /**
     * 内容中等
     */
    int CONTENT_MID_LENGTH = 2048;
    /**
     * 地址一般
     */
    int ADDRESS_LENGTH = 256;
    /**
     * 地址长
     */
    int ADDRESS_LONG_LENGTH = 1024;
    /**
     * 备注
     */
    int REMARK_LENGTH = 256;
    /**
     * 人名
     */
    int REALNAME_LENGTH = 16;
}
