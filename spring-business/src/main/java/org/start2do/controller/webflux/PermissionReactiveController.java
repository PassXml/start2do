package org.start2do.controller.webflux;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.start2do.controller.AbsPermissionController;
import org.start2do.dto.Permission;
import org.start2do.dto.permission.PermissionDto;

@RestController
@RequestMapping("/permission")
@ConditionalOnWebApplication(type = Type.REACTIVE)
@RequiredArgsConstructor
public class PermissionReactiveController implements AbsPermissionController {

    private final org.springframework.web.reactive.result.method.RequestMappingInfoHandlerMapping webfluxRequestMappingHandlerMapping;

    @GetMapping("/allUrls")
    public Set<PermissionDto> getAllUrls() {
        Set<PermissionDto> urls = new HashSet<>();
        Map<RequestMappingInfo, HandlerMethod> map = webfluxRequestMappingHandlerMapping.getHandlerMethods();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : map.entrySet()) {
            RequestMappingInfo info = entry.getKey();
            HandlerMethod handlerMethod = entry.getValue();
            Set<String> patterns = info.getPatternsCondition().getPatterns().stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
            // 获取方法上的注解
            Permission annotations = handlerMethod.getMethodAnnotation(Permission.class);
            if (annotations == null) {
                urls.add(new PermissionDto(patterns, false));
            } else {
                urls.add(new PermissionDto(patterns, annotations.defaultPass()));
            }
        }
        return urls;
    }
}
