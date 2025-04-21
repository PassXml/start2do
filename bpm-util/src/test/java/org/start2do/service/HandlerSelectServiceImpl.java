package org.start2do.service;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.dromara.warm.flow.core.dto.FlowPage;
import org.dromara.warm.flow.ui.dto.HandlerQuery;
import org.dromara.warm.flow.ui.service.HandlerSelectService;
import org.dromara.warm.flow.ui.vo.HandlerAuth;
import org.dromara.warm.flow.ui.vo.HandlerSelectVo;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HandlerSelectServiceImpl implements HandlerSelectService {

    @Override
    public List<String> getHandlerType() {
        return List.of("用户", "岗位", "角色", "脚本", "表达式");
    }

    @Override
    public HandlerSelectVo getHandlerSelect(HandlerQuery req) {
        log.info("{}", req.getHandlerType());
        HandlerSelectVo vo = new HandlerSelectVo();
        if ("表达式".equals(req.getHandlerType())) {
            vo.setHandlerAuths(new FlowPage<>(List.of(
                new HandlerAuth().setHandlerCode("${starter}").setStorageId("${starter}").setGroupName("表达式")
                    .setHandlerName("发起人")),
                0));
            vo.setTreeSelections(List.of());
        }
        return vo;
    }
}
