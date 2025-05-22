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
@Permission(groupName = "权限管理")
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
            Permission methodAnnotation = handlerMethod.getMethodAnnotation(Permission.class);

            boolean defaultPass = (methodAnnotation != null) ? methodAnnotation.defaultPass() : false;
            PermissionDto permissionDto = new PermissionDto(patterns, defaultPass);

            String groupNameValue;
            // 获取类上的注解
            Permission classAnnotation = handlerMethod.getBeanType().getAnnotation(Permission.class);

            // 优先获取Class上面Permission的groupName
            if (classAnnotation != null && classAnnotation.groupName() != null && !classAnnotation.groupName().trim().isEmpty()) {
                groupNameValue = classAnnotation.groupName();
            } else {
                // 如果Class的groupName为空, 则获取方法上面的GroupName
                if (methodAnnotation != null && methodAnnotation.groupName() != null && !methodAnnotation.groupName().trim().isEmpty()) {
                    groupNameValue = methodAnnotation.groupName();
                } else {
                    // 如果Class和方法的groupName都为空, 那么设置当前Controller的ClassName为groupName
                    groupNameValue = handlerMethod.getBeanType().getSimpleName();
                }
            }
            permissionDto.setGroupName(groupNameValue);
            urls.add(permissionDto);
        }
        return urls;
    }
}
