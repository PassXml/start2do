package org.start2do.dto.resp.log;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Data
@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class LogPageResp {

    /**
     * id
     */
    private Integer id;

    /**
     * type
     */
    private String type;
    /**
     * title
     */
    private String title;
    /**
     * remoteAddr
     */
    private String remoteAddr;
    /**
     * userAgent
     */
    private String userAgent;
    /**
     * requestUri
     */
    private String requestUri;
    /**
     * method
     */
    private String method;
    /**
     * params
     */
    private String params;
    /**
     * 请求的Body
     */
    private String requestBody;
    /**
      *
     */
    private String responseBody;
    /**
     * time
     */
    private Long useTime;
    /**
     * exception
     */
    private String exceptionInfo;

    private String createPerson;
    /**
     * createAt
     */
    private LocalDateTime createTime;
    /**
     * updateBy
     */
    private String updatePerson;
    /**
     * updateAt
     */
    private LocalDateTime updateTime;
}
