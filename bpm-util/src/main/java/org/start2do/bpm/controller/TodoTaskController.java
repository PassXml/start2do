package org.start2do.bpm.controller;

import lombok.RequiredArgsConstructor;
import org.dromara.warm.flow.core.service.TaskService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 代办任务
 */
@RestController
@RequestMapping("task/todo")
@RequiredArgsConstructor
public class TodoTaskController {

    private final TaskService taskService;
}
