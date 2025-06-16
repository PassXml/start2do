package org.start2do.cep.action;

import java.io.Serializable;
import org.start2do.cep.dto.PatternMatchResult;

/**
 * 动作执行器接口
 * 所有在规则匹配后需要执行的动作都应实现此接口。
 */
public interface IAction extends Serializable {

    /**
     * 定义动作的唯一类型标识符。
     * 这个字符串应与CEPRule中定义的action字段相匹配。
     *
     * @return 动作的类型字符串（建议小写）
     */
    String type();

    /**
     * 执行具体动作的逻辑。
     *
     * @param result 包含匹配事件和规则信息的模式匹配结果
     */
    void execute(PatternMatchResult result);
}
