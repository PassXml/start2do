package org.start2do.controller.webflux;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.dto.R;
import org.start2do.dto.permission.PermissionAssignRequest;
import org.start2do.service.IPermissionService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/permission")
@ConditionalOnWebApplication(type = Type.REACTIVE)
@RequiredArgsConstructor
public class PermissionAssignReactiveController {

    private final IPermissionService permissionService;
    
    @PostMapping("/assign")
    public Mono<R<Boolean>> assignPermission(@RequestBody PermissionAssignRequest request) {
        boolean result = permissionService.assignPermission(request);
        return Mono.just(R.ok(result));
    }
    
    @PostMapping("/assign/users/{permissionId}")
    public Mono<R<Boolean>> assignToUsers(@PathVariable String permissionId, @RequestBody List<String> userIds) {
        boolean result = permissionService.assignToUsers(permissionId, userIds);
        return Mono.just(R.ok(result));
    }
    
    @PostMapping("/assign/roles/{permissionId}")
    public Mono<R<Boolean>> assignToRoles(@PathVariable String permissionId, @RequestBody List<String> roleIds) {
        boolean result = permissionService.assignToRoles(permissionId, roleIds);
        return Mono.just(R.ok(result));
    }
    
    @DeleteMapping("/remove/users/{permissionId}")
    public Mono<R<Boolean>> removeUserPermission(@PathVariable String permissionId, @RequestBody List<String> userIds) {
        boolean result = permissionService.removeUserPermission(permissionId, userIds);
        return Mono.just(R.ok(result));
    }
    
    @DeleteMapping("/remove/roles/{permissionId}")
    public Mono<R<Boolean>> removeRolePermission(@PathVariable String permissionId, @RequestBody List<String> roleIds) {
        boolean result = permissionService.removeRolePermission(permissionId, roleIds);
        return Mono.just(R.ok(result));
    }
}
