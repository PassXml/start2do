package org.start2do.cep.sink;


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.functions.sink.legacy.SinkFunction;
import org.start2do.cep.action.IAction;
import org.start2do.cep.dto.PatternMatchResult;

@Slf4j
public class ResultSink implements SinkFunction<PatternMatchResult>, Serializable {

    @Getter
    private Map<String, List<IAction>> map = new ConcurrentHashMap<>(10);


    // 结果队列，用于其他组件获取结果
    private static final BlockingQueue<PatternMatchResult> resultQueue = new LinkedBlockingQueue<>();

    @Override
    public void invoke(PatternMatchResult result, Context context) throws Exception {
        try {
            // 记录日志
            log.info("模式匹配结果: {}", result);
            // 将结果放入队列
            resultQueue.offer(result);
            // 执行相应动作
            executeAction(result);
        } catch (Exception e) {
            log.error("处理匹配结果失败", e);
        }
    }

    /**
     * 执行匹配后的动作
     */
    private void executeAction(PatternMatchResult result) {
        String action = result.getAction();
        if (action == null || action.trim().isEmpty()) {
            return;
        }
        List<IAction> actions = map.get(action.toLowerCase());
        if (actions == null) {
            log.warn("没有对应执行器,{}", action);
        }
        for (IAction executor : actions) {
            try {
                executor.execute(result);
            } catch (Exception e) {
                log.error("执行动作失败,{}", result, e);
            }
        }

    }


    /**
     * 获取结果队列（供外部组件使用）
     */
    public static BlockingQueue<PatternMatchResult> getResultQueue() {
        return resultQueue;
    }

}
