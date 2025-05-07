package org.start2do.controller.servlet;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.start2do.controller.AbsPermissionController;

@RestController
@RequestMapping("/permission")
@ConditionalOnWebApplication(type = Type.SERVLET)
@RequiredArgsConstructor
public class PermissionMVCController implements AbsPermissionController {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    @GetMapping("/allUrls")
    public Set<String> getAllUrls() {
        Set<String> urls = new HashSet<>();
        Map<RequestMappingInfo, HandlerMethod> map = requestMappingHandlerMapping.getHandlerMethods();
        for (RequestMappingInfo info : map.keySet()) {
            Set<String> patterns = info.getPatternsCondition().getPatterns();
            urls.addAll(patterns);
        }
        return urls;
    }
}
