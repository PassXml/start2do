package org.start2do.cep.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize
@com.fasterxml.jackson.databind.annotation.JsonSerialize
@ToString
public class CEPRule implements Serializable {

    private static final long serialVersionUID = 1L;

    private String ruleId;
    private String ruleName;
    private String description;
    private boolean enabled;
    private List<PatternStep> pattern;
    private Map<String, Object> conditions;
    private long timeWindowSeconds;
    private String action;
    private Map<String, Object> actionBody;

    // 模式步骤定义
    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    @com.fasterxml.jackson.databind.annotation.JsonSerialize
    @JsonSerialize
    public static class PatternStep implements Serializable {

        private static final long serialVersionUID = 1L;

        private String stepName;
        private String eventType;
        private Map<String, Object> filters;
        private PatternQuantifier quantifier;
        private long timeoutSeconds;

    }


}
