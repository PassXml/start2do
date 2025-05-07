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

@RestController
@RequestMapping("/permission")
@ConditionalOnWebApplication(type = Type.REACTIVE)
@RequiredArgsConstructor
public class PermissionReactiveController implements AbsPermissionController {

    private final org.springframework.web.reactive.result.method.RequestMappingInfoHandlerMapping webfluxRequestMappingHandlerMapping;

    @GetMapping("/allUrls")
    public Set<String> getAllUrls() {
        Map<org.springframework.web.reactive.result.method.RequestMappingInfo, HandlerMethod> map = webfluxRequestMappingHandlerMapping.getHandlerMethods();
        Set<String> urls = new HashSet<>();
        for (RequestMappingInfo info : map.keySet()) {
            Set<String> patterns = info.getPatternsCondition().getPatterns().stream().map(Object::toString)
                .collect(Collectors.toSet());
            urls.addAll(patterns);
        }
        return urls;
    }
}
