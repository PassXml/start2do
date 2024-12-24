package org.start2do.dto.resp.log;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.util.ExcelUtil.ExcelSetting;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class LogExcelPojo {

    /**
     * 编号
     */
    @ExcelSetting(value = "编号")
    private Long id;

    /**
     * 日志类型
     */
    @ExcelSetting(value = "日志类型")
    private String type;

    /**
     * 日志标题
     */
    @ExcelSetting(value = "日志标题")
    private String title;

    /**
     * 操作IP地址
     */
    @ExcelSetting(value = "操作IP地址")
    private String remoteAddr;

    /**
     * 用户浏览器
     */
    @ExcelSetting(value = "用户浏览器")
    private String userAgent;

    /**
     * 请求URI
     */
    @ExcelSetting(value = "请求URI")
    private String requestUri;

    /**
     * 操作方式
     */
    @ExcelSetting(value = "操作方式")
    private String method;

    /**
     * 操作提交的数据
     */
    @ExcelSetting(value = "操作提交的数据")
    private String params;

    @ExcelSetting(value = "请求头")
    private String requestHeader;

    @ExcelSetting(value = "请求体")
    private String requestBody;

    @ExcelSetting(value = "响应体")
    private String responseBody;

    @ExcelSetting(value = "响应头")
    private String responseHeader;

    /**
     * 执行时间
     */
    @ExcelSetting(value = "执行时间")
    private Long useTime;

    /**
     * 异常信息
     */
    @ExcelSetting(value = "异常信息")
    private String exceptionInfo;

    /**
     * 创建时间
     */
    @ExcelSetting(value = "创建时间")
    private LocalDateTime createTime;

    @ExcelSetting(value = "创建人员")
    private String createPerson;

    /**
     * 更新时间
     */
    @ExcelSetting(value = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 更新人员
     */
    @ExcelSetting(value = "更新人员")
    private String updatePerson;

    @ExcelSetting(value = "版本")
    private Long version;
}
