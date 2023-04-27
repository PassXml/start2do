package org.start2do.entity.business;

import com.alibaba.excel.annotation.ExcelProperty;
import io.ebean.Model;
import io.ebean.annotation.DbComment;
import io.ebean.annotation.DbDefault;
import io.ebean.annotation.DbEnumValue;
import io.ebean.annotation.Identity;
import io.ebean.annotation.IdentityGenerated;
import io.ebean.annotation.IdentityType;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.dto.BusinessException;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@Table(name = "sys_log")
public class SysLog extends Model {

    /**
     * 编号
     */
    @Id
    @Identity(type = IdentityType.IDENTITY,generated = IdentityGenerated.BY_DEFAULT)
    @ExcelProperty("日志编号")
    @DbComment("日志编号")
    private Long id;

    /**
     * 日志类型
     */
    @NotBlank(message = "日志类型不能为空")
    @ExcelProperty("日志类型")
    @DbComment("日志类型（0-正常 9-错误）")
    private Type type;

    /**
     * 日志标题
     */
    @DbComment("日志标题")
    @ExcelProperty("日志标题")
    @Column(nullable = false, length = 512)
    private String title;

    /**
     * 操作IP地址
     */
    @ExcelProperty("操作ip地址")
    @DbComment("操作ip地址")
    private String remoteAddr;

    /**
     * 用户浏览器
     */
    @ExcelProperty("用户浏览器")
    @DbComment("用户浏览器")
    private String userAgent;

    /**
     * 请求URI
     */
    @DbComment("请求uri")
    @ExcelProperty("请求uri")
    private String requestUri;

    /**
     * 操作方式
     */
    @ExcelProperty("操作方式")
    @DbComment("操作方式")
    private String method;

    /**
     * 操作提交的数据
     */
    @Lob
    @ExcelProperty("操作提交的数据")
    @DbComment("数据")
    private String params;
    @Lob
    @ExcelProperty(("请求头"))
    private String requestHeader;
    @Lob
    @ExcelProperty(("请求体"))
    private String requestBody;
    @Lob
    @ExcelProperty("返回体")
    private String responseBody;
    @Lob
    @ExcelProperty("Resp头")
    private String responseHeader;

    /**
     * 执行时间
     */
    @ExcelProperty("方法执行时间")
    @DbComment("方法执行时间")
    private Long useTime;

    /**
     * 异常信息
     */
    @Lob
    @DbComment("异常信息")
    @ExcelProperty("异常信息")
    @Column(name = "exception_info")
    private String exceptionInfo;

    /**
     * 创建时间
     */
    @WhenCreated
    @ExcelProperty("创建时间")
    @Column(name = "create_time")
    private LocalDateTime createTime;
    @DbDefault("NO SET")
    @ExcelProperty("创建人")
    @Column(name = "create_person")
    private String createPerson;

    /**
     * 更新时间
     */
    @WhenModified
    @ExcelProperty("更新时间")
    @Column(name = "update_time")
    private LocalDateTime updateTime;


    /**
     * 更新人员
     */
    @ExcelProperty("更新人")
    @DbDefault("NO SET")
    @Column(name = "update_person")
    private String updatePerson;

    @Version

    @ExcelProperty("版本号")
    @Column(name = "version")
    private Long version;


    public enum Type {
        Error("9", "异常"), Info("0", "正常");
        private String value;
        private String label;

        Type(String value, String label) {
            this.value = value;
            this.label = label;
        }

        @DbEnumValue(length = 2)
        public String getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }

        public static Type find(String s) {
            for (Type value : values()) {
                if (value.getValue().equals(s)) {
                    return value;
                }
            }
            throw new BusinessException(String.format("%s未知字典值:%s", "Type", s));
        }

        @Override
        public String toString() {
            return String.join("", label, "(", value, ")");
        }
    }
}
