# Flink CEP 工具使用指南

## 概述

这个基于Flink的CEP工具类提供了完整的事件处理功能，包括：

- **多事件源支持**：HTTP、队列、Kafka
- **动态规则管理**：运行时添加、更新、删除规则
- **灵活的回调机制**：HTTP回调、自定义逻辑回调
- **实时模式匹配**：基于Flink CEP引擎的高性能处理

## 核心组件

### 1. FlinkCEPTool

主要的工具类，提供CEP处理的统一接口。

```java
@Autowired
private FlinkCEPTool flinkCEPTool;

// 初始化
flinkCEPTool.initialize();

// 启动处理
flinkCEPTool.start();

// 输入事件
Event event = new Event()
    .setEventId("event_001")
    .setEventType("login_failure")
    .setSource("auth_service")
    .setPayload(Map.of("userId", "user123", "status", "failed"))
    .setTimestamp(LocalDateTime.now());

flinkCEPTool.inputQueueEvent(event);
```

### 2. 事件源

#### HTTP事件源
```java
// 通过HTTP API输入事件（需要启用HTTP配置）
flinkCEPTool.inputHttpEvent(event);
```

#### 队列事件源
```java
// 单个事件输入
flinkCEPTool.inputQueueEvent(event);

// 带超时的事件输入
flinkCEPTool.inputQueueEventWithTimeout(event, 5, TimeUnit.SECONDS);

// 批量事件输入
List<Event> events = Arrays.asList(event1, event2, event3);
flinkCEPTool.inputQueueEventsBatch(events);
```

#### Kafka事件源
通过配置文件启用，自动消费Kafka消息。

### 3. 规则管理

#### 创建规则
```java
// 创建模式步骤
CEPRule.PatternStep step1 = new CEPRule.PatternStep()
    .setStepName("first_failure")
    .setEventType("login_failure")
    .setFilters(Map.of("status", "failed"))
    .setQuantifier(CEPRule.PatternQuantifier.ONE);

// 创建规则
Map<String, Object> actionBody = new HashMap<>();
actionBody.put("callbackType", "alert_callback");
actionBody.put("priority", "HIGH");

CEPRule rule = new CEPRule()
    .setRuleId("login_failure_3_times")
    .setRuleName("连续失败登录检测")
    .setDescription("检测用户连续3次登录失败")
    .setEnabled(true)
    .setPattern(Arrays.asList(step1, step2, step3))
    .setTimeWindowSeconds(300)
    .setAction("custom_callback")
    .setActionBody(actionBody);

// 添加规则
flinkCEPTool.addRule(rule);
```

#### 动态规则操作
```java
// 更新规则
flinkCEPTool.updateRule(updatedRule);

// 删除规则
flinkCEPTool.deleteRule("rule_id");

// 启用/禁用规则
flinkCEPTool.setRuleEnabled("rule_id", true);

// 获取所有活跃规则
List<CEPRule> rules = flinkCEPTool.getActiveRules();
```

### 4. 回调机制

#### HTTP回调
```java
// 配置HTTP回调
Map<String, Object> actionBody = new HashMap<>();
actionBody.put("callbackUrl", "http://localhost:8080/api/alerts");
actionBody.put("httpMethod", "POST");
actionBody.put("headers", Map.of("Authorization", "Bearer token123"));

CEPRule rule = new CEPRule()
    .setAction("http_callback")
    .setActionBody(actionBody);
```

#### 自定义逻辑回调
```java
// 注册自定义回调
flinkCEPTool.registerCallback("alert_callback", result -> {
    log.warn("触发警报: {}", result.getRuleName());
    // 处理警报逻辑
});

// 在规则中使用自定义回调
Map<String, Object> actionBody = new HashMap<>();
actionBody.put("callbackType", "alert_callback");

CEPRule rule = new CEPRule()
    .setAction("custom_callback")
    .setActionBody(actionBody);
```

## 配置示例

### application.yml
```yaml
flink:
  job-name: "CEP-Processing-Job"
  parallelism: 2
  enable-checkpoint: true
  checkpoint-interval: 5000
  
  http:
    enabled: true
    port: 8081
    context-path: "/cep"
    
  kafka:
    enabled: true
    bootstrap-servers: "localhost:9092"
    topic: "cep-events"
    group-id: "cep-group"
```

## 完整使用示例

### 1. 创建规则管理器
```java
@Service
public class CEPService {
    
    private final FlinkCEPTool flinkCEPTool;
    
    public CEPService(FlinkCEPTool flinkCEPTool) {
        this.flinkCEPTool = flinkCEPTool;
    }
    
    @PostConstruct
    public void init() {
        // 初始化CEP工具
        flinkCEPTool.initialize();
        
        // 注册自定义回调
        registerCallbacks();
        
        // 创建初始规则
        createInitialRules();
        
        // 启动处理
        flinkCEPTool.start();
    }
    
    private void registerCallbacks() {
        // 注册警报回调
        flinkCEPTool.registerCallback("security_alert", result -> {
            // 处理安全警报
            sendSecurityAlert(result);
        });
        
        // 注册业务回调
        flinkCEPTool.registerCallback("business_event", result -> {
            // 处理业务事件
            processBusinessEvent(result);
        });
    }
    
    private void createInitialRules() {
        try {
            // 创建安全检测规则
            CEPRule securityRule = createSecurityRule();
            flinkCEPTool.addRule(securityRule);
            
            // 创建业务规则
            CEPRule businessRule = createBusinessRule();
            flinkCEPTool.addRule(businessRule);
            
        } catch (Exception e) {
            log.error("创建初始规则失败", e);
        }
    }
    
    // ... 其他方法
}
```

### 2. 事件输入服务
```java
@Service
public class EventInputService {
    
    private final FlinkCEPTool flinkCEPTool;
    
    public EventInputService(FlinkCEPTool flinkCEPTool) {
        this.flinkCEPTool = flinkCEPTool;
    }
    
    // 处理用户登录事件
    public void processLoginEvent(String userId, String status, String ip) {
        Event event = new Event()
            .setEventId(UUID.randomUUID().toString())
            .setEventType("login_event")
            .setSource("auth_service")
            .setPayload(Map.of(
                "userId", userId,
                "status", status,
                "ip", ip,
                "timestamp", System.currentTimeMillis()
            ))
            .setTimestamp(LocalDateTime.now());
        
        flinkCEPTool.inputQueueEvent(event);
    }
    
    // 处理交易事件
    public void processTradeEvent(String userId, String symbol, double quantity, double price) {
        Event event = new Event()
            .setEventId(UUID.randomUUID().toString())
            .setEventType("trade_event")
            .setSource("trading_service")
            .setPayload(Map.of(
                "userId", userId,
                "symbol", symbol,
                "quantity", quantity,
                "price", price,
                "timestamp", System.currentTimeMillis()
            ))
            .setTimestamp(LocalDateTime.now());
        
        flinkCEPTool.inputQueueEvent(event);
    }
}
```

### 3. 规则更新控制器
```java
@RestController
@RequestMapping("/api/rules")
public class RuleController {
    
    private final FlinkCEPTool flinkCEPTool;
    
    public RuleController(FlinkCEPTool flinkCEPTool) {
        this.flinkCEPTool = flinkCEPTool;
    }
    
    // 添加规则
    @PostMapping
    public ResponseEntity<String> addRule(@RequestBody CEPRule rule) {
        try {
            flinkCEPTool.addRule(rule);
            return ResponseEntity.ok("规则添加成功");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("规则添加失败: " + e.getMessage());
        }
    }
    
    // 更新规则
    @PutMapping("/{ruleId}")
    public ResponseEntity<String> updateRule(@PathVariable String ruleId, @RequestBody CEPRule rule) {
        try {
            rule.setRuleId(ruleId);
            flinkCEPTool.updateRule(rule);
            return ResponseEntity.ok("规则更新成功");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("规则更新失败: " + e.getMessage());
        }
    }
    
    // 删除规则
    @DeleteMapping("/{ruleId}")
    public ResponseEntity<String> deleteRule(@PathVariable String ruleId) {
        try {
            flinkCEPTool.deleteRule(ruleId);
            return ResponseEntity.ok("规则删除成功");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("规则删除失败: " + e.getMessage());
        }
    }
    
    // 获取所有规则
    @GetMapping
    public ResponseEntity<List<CEPRule>> getAllRules() {
        List<CEPRule> rules = flinkCEPTool.getActiveRules();
        return ResponseEntity.ok(rules);
    }
    
    // 启用/禁用规则
    @PutMapping("/{ruleId}/enabled/{enabled}")
    public ResponseEntity<String> setRuleEnabled(@PathVariable String ruleId, @PathVariable boolean enabled) {
        try {
            flinkCEPTool.setRuleEnabled(ruleId, enabled);
            return ResponseEntity.ok("规则状态更新成功");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("规则状态更新失败: " + e.getMessage());
        }
    }
}
```

## 高级特性

### 1. 批量操作
```java
// 批量添加规则
List<CEPRule> rules = Arrays.asList(rule1, rule2, rule3);
flinkCEPTool.addRules(rules);

// 批量输入事件
List<Event> events = Arrays.asList(event1, event2, event3);
flinkCEPTool.inputQueueEventsBatch(events);
```

### 2. 队列状态监控
```java
// 获取队列状态
int queueSize = flinkCEPTool.getHttpEventQueueSize();
int remainingCapacity = QueueEventSource.getRemainingCapacity();
boolean isEmpty = QueueEventSource.isQueueEmpty();

// 清空队列
QueueEventSource.clearQueue();
```

### 3. 规则变更监听
```java
// 注册规则变更监听器
flinkCEPTool.addRuleChangeListener(new DynamicRuleManagementService.RuleChangeListener() {
    @Override
    public void onRuleAdded(CEPRule rule) {
        log.info("规则已添加: {}", rule.getRuleId());
    }
    
    @Override
    public void onRuleUpdated(CEPRule rule) {
        log.info("规则已更新: {}", rule.getRuleId());
    }
    
    @Override
    public void onRuleDeleted(String ruleId) {
        log.info("规则已删除: {}", ruleId);
    }
});
```

### 4. 自动重载规则
```java
// 启用自动重载
flinkCEPTool.enableAutoReload(30000); // 30秒

// 手动重载
flinkCEPTool.reloadRules();

// 禁用自动重载
flinkCEPTool.disableAutoReload();
```

## 注意事项

1. **线程安全**：所有公共方法都是线程安全的，可以在多线程环境中使用
2. **性能考虑**：合理设置并行度和检查点间隔，避免过度消耗资源
3. **错误处理**：所有操作都包含异常处理，建议在调用时进行适当的错误处理
4. **资源清理**：应用停止时调用 `flinkCEPTool.stop()` 进行资源清理

## 常见问题

### Q: 如何处理事件丢失？
A: 启用Flink检查点机制，配置合适的检查点间隔，确保事件处理的可恢复性。

### Q: 规则更新的性能影响？
A: 规则更新会实时生效，但频繁更新可能会影响性能。建议批量更新规则。

### Q: 如何优化事件处理性能？
A: 
- 调整Flink并行度
- 使用合适的分区策略
- 优化规则复杂度
- 配置适当的缓冲区大小

### Q: 如何监控CEP处理状态？
A: 通过队列状态监控、规则变更监听和Flink的Web UI来监控处理状态。