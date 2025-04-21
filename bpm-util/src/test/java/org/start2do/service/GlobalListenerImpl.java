package org.start2do.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.warm.flow.core.FlowEngine;
import org.dromara.warm.flow.core.dto.DefJson;
import org.dromara.warm.flow.core.dto.FlowParams;
import org.dromara.warm.flow.core.dto.NodeJson;
import org.dromara.warm.flow.core.entity.Instance;
import org.dromara.warm.flow.core.enums.SkipType;
import org.dromara.warm.flow.core.listener.GlobalListener;
import org.dromara.warm.flow.core.listener.ListenerVariable;
import org.dromara.warm.flow.core.service.InsService;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.stereotype.Service;
import org.start2do.bpm.service.IUserHandle;

/**
 * 全局监听器
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GlobalListenerImpl implements GlobalListener {

    private final InsService insService;
    private final IUserHandle iUserHandle;
    public static String SYSTEM_HANDLE = "[system]";

    @Override
    public void assignment(ListenerVariable vars) {
        Instance instance = vars.getInstance();
        log.info("分派监听器,{},{}", instance.getId(), vars.getVariable());
        String nodeCode = instance.getNodeCode();
        String defJsonStr = instance.getDefJson();
        if (StringUtils.isNotBlank(defJsonStr)) {
            DefJson defJson = FlowEngine.jsonConvert.strToBean(defJsonStr, DefJson.class);
            for (NodeJson nodeJson : defJson.getNodeList()) {
                if (nodeJson.getNodeCode().equals(nodeCode)) {
                    String handler = vars.getFlowParams().getHandler();
                    Map<String, Object> extMap = nodeJson.getExtMap();
                    if (SYSTEM_HANDLE.equals(handler)) {
                        extMap.put("办理人", "系统");
                    } else if (handler != null) {
                        extMap.put("办理人", iUserHandle.getUserHandler(handler));
                    }
                    extMap.put("完成时间",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss")));


                }
            }
            instance.setDefJson(FlowEngine.jsonConvert.objToStr(defJson));
        }
    }

    @Override
    public void finish(ListenerVariable vars) {
        Instance instance = vars.getInstance();
        String nodeName = instance.getNodeName();
        String businessId = instance.getBusinessId();
        String status = instance.getFlowStatus();
        log.info("完成通知,,{},{},{}", businessId, nodeName, status);
        Map<String, Object> variable = vars.getVariable();
        String nodeSkip = nodeName + "_autoSkip";
        Object autoSkin = variable.getOrDefault(nodeSkip, "false");
        if ("true".equals(autoSkin)) {
            variable.put(nodeSkip, "false");
            log.info("自动通过");
            FlowParams params = new FlowParams().ignore(true).skipType(SkipType.PASS.getKey()).message("自动通过")
                .variable(variable).handler(SYSTEM_HANDLE);
            insService.skipByInsId(instance.getId(), params);
        }
    }

    @Override
    public void create(ListenerVariable listenerVariable) {
        log.info("创建");
    }

}
