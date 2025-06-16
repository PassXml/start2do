package org.start2do.cep.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.start2do.cep.dto.CEPRule;

@Slf4j
@Service
public class FileRuleStorageService implements RuleStorageService {

    private final File rulesFile;
    private final ObjectMapper objectMapper;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public FileRuleStorageService(
        @Value("${cep.rules.file.path:./cep-rules.json}") String filePath,
        ObjectMapper objectMapper) {
        this.rulesFile = new File(filePath);
        this.objectMapper = objectMapper.copy();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        log.info("规则文件存储路径: {}", this.rulesFile.getAbsolutePath());
    }

    @Override
    public List<CEPRule> loadRules() throws IOException {
        lock.readLock().lock();
        try {
            if (!rulesFile.exists() || rulesFile.length() == 0) {
                log.warn("规则文件不存在或为空: {}", rulesFile.getAbsolutePath());
                return new ArrayList<>();
            }
            byte[] jsonData = Files.readAllBytes(rulesFile.toPath());
            return objectMapper.readValue(jsonData, new TypeReference<List<CEPRule>>() {});
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void saveRule(CEPRule rule) throws IOException {
        lock.writeLock().lock();
        try {
            List<CEPRule> rules = new ArrayList<>(loadRules());
            boolean updated = false;
            for (int i = 0; i < rules.size(); i++) {
                if (rules.get(i).getRuleId().equals(rule.getRuleId())) {
                    rules.set(i, rule);
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                rules.add(rule);
            }
            writeToFile(rules);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void deleteRule(String ruleId) throws IOException {
        lock.writeLock().lock();
        try {
            List<CEPRule> rules = new ArrayList<>(loadRules());
            List<CEPRule> updatedRules = rules.stream()
                .filter(rule -> !rule.getRuleId().equals(ruleId))
                .collect(Collectors.toList());

            if (rules.size() != updatedRules.size()) {
                writeToFile(updatedRules);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void writeToFile(List<CEPRule> rules) throws IOException {
        objectMapper.writeValue(rulesFile, rules);
    }
}
