package org.start2do.cep.sink;


import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.start2do.cep.action.IAction;
import org.start2do.cep.dto.PatternMatchResult;

@Slf4j
public class ResultSink implements Sink<PatternMatchResult> {

    // 结果队列，用于其他组件获取结果
    private static final BlockingQueue<PatternMatchResult> resultQueue = new LinkedBlockingQueue<>();

    private final Map<String, List<IAction>> actionMap;

    /**
     * 构造函数
     *
     * @param actionMap 操作映射，键是动作名称，值是IAction执行器列表
     */
    public ResultSink(Map<String, List<IAction>> actionMap) {
        this.actionMap = actionMap;
    }

    @Override
    public SinkWriter<PatternMatchResult> createWriter(InitContext context) {
        return new ResultSinkWriter(actionMap);
    }

    /**
     * 获取结果队列（供外部组件使用）
     */
    public static BlockingQueue<PatternMatchResult> getResultQueue() {
        return resultQueue;
    }

    /**
     * 内部SinkWriter实现，负责处理实际的写入逻辑
     */
    private static class ResultSinkWriter implements SinkWriter<PatternMatchResult>, Serializable {

        private final Map<String, List<IAction>> actionMap;

        public ResultSinkWriter(Map<String, List<IAction>> actionMap) {
            this.actionMap = actionMap;
        }

        @Override
        public void write(PatternMatchResult result, Context context) {
            try {
                // 记录日志
                log.info("模式匹配结果: {}", result);
                // 将结果放入队列
                ResultSink.resultQueue.offer(result);
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
            List<IAction> actions = actionMap.get(action.toLowerCase());
            if (actions == null || actions.isEmpty()) {
                log.warn("没有对应执行器,{}", action);
                return;
            }
            for (IAction executor : actions) {
                try {
                    executor.execute(result);
                } catch (Exception e) {
                    log.error("执行动作失败,{}", result, e);
                }
            }
        }

        @Override
        public void flush(boolean endOfInput) {
            // 对于这个Sink，我们不进行缓冲，所以flush是无操作
        }

        @Override
        public List<Void> prepareCommit() {
            // 非事务性Sink，不需要实现
            return Collections.emptyList();
        }

        @Override
        public void close() {
            // 没有需要关闭的资源
        }
    }
}
