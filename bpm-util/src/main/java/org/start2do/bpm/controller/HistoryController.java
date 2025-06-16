package org.start2do.bpm.controller;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.warm.flow.core.entity.HisTask;
import org.dromara.warm.flow.core.service.HisTaskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.bpm.dto.history.HistoryItemResp;
import org.start2do.dto.R;

@Slf4j
@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HisTaskService hisTaskService;

    /**
     * 查询历史
     *
     * @param taskId
     * @return
     */
    @GetMapping("list")
    public R<List<HisTask>> list(Long taskId) {
        return R.ok(hisTaskService.listByTaskIdAndCooperateTypes(taskId));
    }

    /**
     * 查询历史
     *
     * @return
     */
    @GetMapping("listByInstanceId")
    public R<List<HistoryItemResp>> listByInstanceId(Long instanceId) {
        List<HisTask> hisTasks = hisTaskService.getByInsAndNodeCodes(instanceId, List.of());
        List<HistoryItemResp> result = new ArrayList<>();
        for (HisTask task : hisTasks) {
            HistoryItemResp resp = new HistoryItemResp(task);
            result.add(resp);
            if ("结束".equals(task.getTargetNodeName())) {
                result.add(new HistoryItemResp(task).setNodeName(task.getTargetNodeName())
                    .setCreateTime(task.getUpdateTime()));
            }
        }
        return R.ok(result);
    }

}
