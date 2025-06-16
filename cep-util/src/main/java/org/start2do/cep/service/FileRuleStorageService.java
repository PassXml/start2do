package org.start2do.cep.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.start2do.cep.dto.CEPRule;
import org.springframework.stereotype.Service;

@Service
public class FileRuleStorageService implements RuleStorageService {
    private static final String STORAGE_DIR = "cep-rules";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void saveRule(CEPRule rule) {
        try {
            Path dirPath = Paths.get(STORAGE_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            Path filePath = dirPath.resolve(rule.getRuleId() + ".json");
            objectMapper.writeValue(filePath.toFile(), rule);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save rule: " + rule.getRuleId(), e);
        }
    }

    @Override
    public CEPRule loadRule(String ruleId) {
        try {
            Path filePath = Paths.get(STORAGE_DIR, ruleId + ".json");
            if (!Files.exists(filePath)) {
                return null;
            }
            return objectMapper.readValue(filePath.toFile(), CEPRule.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load rule: " + ruleId, e);
        }
    }

    @Override
    public Map<String, CEPRule> loadAllRules() {
        try {
            Path dirPath = Paths.get(STORAGE_DIR);
            if (!Files.exists(dirPath)) {
                return Collections.emptyMap();
            }

            Map<String, CEPRule> rules = new HashMap<>();
            Files.list(dirPath)
                .filter(path -> path.toString().endsWith(".json"))
                .forEach(path -> {
                    try {
                        CEPRule rule = objectMapper.readValue(path.toFile(), CEPRule.class);
                        rules.put(rule.getRuleId(), rule);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to load rule from: " + path, e);
                    }
                });
            return rules;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load rules", e);
        }
    }

    @Override
    public void deleteRule(String ruleId) {
        try {
            Path filePath = Paths.get(STORAGE_DIR, ruleId + ".json");
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete rule: " + ruleId, e);
        }
    }
}
