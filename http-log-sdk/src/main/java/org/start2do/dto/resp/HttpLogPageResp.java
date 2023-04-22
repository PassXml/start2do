package org.start2do.dto.resp;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class HttpLogPageResp {

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 路由ID
     */
    private String routeId;
    /**
     * 主机名称
     */
    private String host;
    /**
     * 主机地址
     */
    private String clientIp;
    /**
     * 节点IP
     */
    private String balancerIp;
    /**
     * 请求体
     */
    private String body;
    /**
     * 请求时间
     */
    private Integer timestamp;
    /**
     * 请求路径
     */
    private String uri;
    /**
     * 请求头
     */
    private String requestHeader;
    /**
     * 请求Url参数
     */
    private String reqeustQuery;
    /**
     * 请求体
     */
    private String reqeustBody;
    /**
     * 返回头
     */
    private String responseHeader;
    /**
     * 返回体
     */
    private String responseBody;
    /**
     * 返回状态码
     */
    private Integer responseStatus;
    /**
     * 总耗时
     */
    private Double latency;
    /**
     * 请求耗时
     */
    private Integer upstreamLatency;
}
