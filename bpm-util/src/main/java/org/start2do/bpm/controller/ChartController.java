package org.start2do.bpm.controller;

import java.awt.Color;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.warm.flow.core.chart.BetweenChart;
import org.dromara.warm.flow.core.chart.FlowChart;
import org.dromara.warm.flow.core.service.ChartService;
import org.dromara.warm.flow.core.utils.MapUtil;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 流程图绘制
 */
@Slf4j
@Controller
@RequestMapping("/chart")
@RequiredArgsConstructor
public class ChartController {

    private final ChartService chartService;

    @GetMapping(value = "{instanceId}", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody byte[] flowChart(@PathVariable("instanceId") Long instanceId) {
        String imageBase64 = chartService.chartIns(instanceId, (flowChartChain) -> {
            List<FlowChart> flowChartList = flowChartChain.getFlowChartList();
            for (FlowChart flowChart : flowChartList) {
                if (flowChart instanceof BetweenChart) {
                    BetweenChart betweenChart = (BetweenChart) flowChart;
                    Map<String, Object> extMap = betweenChart.getNodeJson().getExtMap();
                    if (MapUtil.isNotEmpty(extMap)) {
                        for (Entry<String, Object> entry : extMap.entrySet()) {
                            betweenChart.addText(entry.getKey() + ":", Color.red);
                            betweenChart.addText((String) entry.getValue(), Color.red);
                        }
                    }
                }
            }
        });
        return Base64.getDecoder().decode(imageBase64);
    }

}
