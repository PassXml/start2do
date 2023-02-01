package org.start2do.dto.resp.log;

import com.alibaba.excel.annotation.ExcelProperty;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class LogExcelPojo {

    /**
     * 编号
     */
    @ExcelProperty("日志编号")
    private Long id;

    /**
     * 日志类型
     */
    @ExcelProperty("日志类型")
    private String type;

    /**
     * 日志标题
     */
    @ExcelProperty("日志标题")
    private String title;

    /**
     * 操作IP地址
     */
    @ExcelProperty("操作ip地址")
    private String remoteAddr;

    /**
     * 用户浏览器
     */
    @ExcelProperty("用户浏览器")
    private String userAgent;

    /**
     * 请求URI
     */
    @ExcelProperty("请求uri")
    private String requestUri;

    /**
     * 操作方式
     */
    @ExcelProperty("操作方式")
    private String method;

    /**
     * 操作提交的数据
     */
    @ExcelProperty("Query")
    private String params;
    @ExcelProperty(("请求头"))
    private String requestHeader;
    @ExcelProperty(("请求体"))
    private String requestBody;
    @ExcelProperty("返回体")
    private String responseBody;
    @ExcelProperty("Resp头")
    private String responseHeader;

    /**
     * 执行时间
     */
    @ExcelProperty("方法执行时间")
    private Long useTime;

    /**
     * 异常信息
     */
    @ExcelProperty("异常信息")
    private String exceptionInfo;

    /**
     * 创建时间
     */
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
    @ExcelProperty("创建人")
    private String createPerson;

    /**
     * 更新时间
     */
    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;


    /**
     * 更新人员
     */
    @ExcelProperty("更新人")
    private String updatePerson;

    @ExcelProperty("版本号")
    private Long version;
}
