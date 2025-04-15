package org.start2do.service;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.dromara.warm.flow.ui.dto.HandlerQuery;
import org.dromara.warm.flow.ui.service.HandlerSelectService;
import org.dromara.warm.flow.ui.vo.HandlerSelectVo;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HandlerSelectServiceImpl implements HandlerSelectService {

    @Override
    public List<String> getHandlerType() {
        return List.of("用户", "岗位", "角色");
    }

    @Override
    public HandlerSelectVo getHandlerSelect(HandlerQuery handlerQuery) {
        log.info("{}", handlerQuery);
        return null;
    }
}
