package org.start2do.script.config;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.start2do.script.util.ScriptWhiteList;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor

@ConfigurationProperties(prefix = "start2do.script.nashorn")
public class NashornJsConfig {

    private boolean enable = false;
    private int maxPoolSize = 20;
    private String globalScript = "";
    private long maxCPUTime;
    private long maxMemory;
    private boolean enableDB = false;
    private boolean enableJsonOperate = false;
    private boolean enableHTTP = false;
    /**
     * 默认开放常用基础类型、集合、时间类及公共脚本函数，避免脚本直接触达文件、线程、反射、进程等高风险入口。
     */
    private Set<String> whiteList = new LinkedHashSet<>(ScriptWhiteList.defaultWhiteList());
    private Duration expireAfterAccess;

    public static Set<String> defaultWhiteList() {
        return ScriptWhiteList.defaultWhiteList();
    }
}
