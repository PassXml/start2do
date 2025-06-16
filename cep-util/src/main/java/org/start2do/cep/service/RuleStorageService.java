package org.start2do.cep.service;

import java.io.IOException;
import java.util.List;
import org.start2do.cep.dto.CEPRule;

/**
 * 规则存储服务接口
 * 定义了规则的加载、保存和删除操作，以将存储实现与业务逻辑分离。
 */
public interface RuleStorageService {

    /**
     * 从持久化层加载所有规则。
     *
     * @return 规则列表
     * @throws IOException 如果加载过程中发生I/O错误
     */
    List<CEPRule> loadRules() throws IOException;

    /**
     * 保存或更新单个规则。
     * 如果规则已存在，则更新它；否则，添加新规则。
     *
     * @param rule 要保存的规则
     * @throws IOException 如果保存过程中发生I/O错误
     */
    void saveRule(CEPRule rule) throws IOException;

    /**
     * 根据规则ID删除一个规则。
     *
     * @param ruleId 要删除的规则的ID
     * @throws IOException 如果删除过程中发生I/O错误
     */
    void deleteRule(String ruleId) throws IOException;
}
