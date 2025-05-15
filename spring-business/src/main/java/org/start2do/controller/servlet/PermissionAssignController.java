package org.start2do.controller.servlet;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.dto.R;
import org.start2do.dto.permission.PermissionAssignRequest;
import org.start2do.service.IPermissionService;

@RestController
@RequestMapping("/permission")
@RequiredArgsConstructor
public class PermissionAssignController {

    private final IPermissionService permissionService;
    
    @PostMapping("/assign")
    public R<Boolean> assignPermission(@RequestBody PermissionAssignRequest request) {
        boolean result = permissionService.assignPermission(request);
        return R.ok(result);
    }
    
    @PostMapping("/assign/users/{permissionId}")
    public R<Boolean> assignToUsers(@PathVariable String permissionId, @RequestBody List<String> userIds) {
        boolean result = permissionService.assignToUsers(permissionId, userIds);
        return R.ok(result);
    }
    
    @PostMapping("/assign/roles/{permissionId}")
    public R<Boolean> assignToRoles(@PathVariable String permissionId, @RequestBody List<String> roleIds) {
        boolean result = permissionService.assignToRoles(permissionId, roleIds);
        return R.ok(result);
    }
    
    @DeleteMapping("/remove/users/{permissionId}")
    public R<Boolean> removeUserPermission(@PathVariable String permissionId, @RequestBody List<String> userIds) {
        boolean result = permissionService.removeUserPermission(permissionId, userIds);
        return R.ok(result);
    }
    
    @DeleteMapping("/remove/roles/{permissionId}")
    public R<Boolean> removeRolePermission(@PathVariable String permissionId, @RequestBody List<String> roleIds) {
        boolean result = permissionService.removeRolePermission(permissionId, roleIds);
        return R.ok(result);
    }
}
