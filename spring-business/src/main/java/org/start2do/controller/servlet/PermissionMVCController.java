package org.start2do.controller.servlet;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.start2do.controller.AbsPermissionController;
import org.start2do.dto.BusinessException;
import org.start2do.dto.Permission;
import org.start2do.dto.R;
import org.start2do.dto.permission.PermissionDetailResp;
import org.start2do.dto.permission.PermissionDto;
import org.start2do.dto.req.permission.PermissionRoleAddReq;
import org.start2do.dto.req.permission.PermissionUserAddReq;
import org.start2do.dto.resp.permission.PermissionPageResp;
import org.start2do.entity.security.SysPermissionRoleRef;
import org.start2do.entity.security.SysPermissionRoleRefId;
import org.start2do.entity.security.SysPermissionUserRef;
import org.start2do.entity.security.SysPermissionUserRefId;
import org.start2do.entity.security.query.QSysPermissionRoleRef;
import org.start2do.entity.security.query.QSysPermissionUserRef;
import org.start2do.service.IPermissionService;
import org.start2do.util.ListUtil;

@RestController
@RequestMapping("/permission")
@ConditionalOnWebApplication(type = Type.SERVLET)
@RequiredArgsConstructor
@Permission(groupName = "权限管理")
public class PermissionMVCController implements AbsPermissionController {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final IPermissionService permissionService;

    public Set<PermissionDto> getAllUrls() {
        Set<PermissionDto> urls = new HashSet<>();
        Map<RequestMappingInfo, HandlerMethod> map = requestMappingHandlerMapping.getHandlerMethods();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : map.entrySet()) {
            RequestMappingInfo info = entry.getKey();
            HandlerMethod handlerMethod = entry.getValue();
            Set<String> patterns = info.getPatternsCondition().getPatterns();
            // 获取方法上的注解
            Permission methodAnnotation = handlerMethod.getMethodAnnotation(Permission.class);

            boolean defaultPass = (methodAnnotation != null) ? methodAnnotation.defaultPass() : false;
            PermissionDto permissionDto = new PermissionDto(patterns, defaultPass);

            String groupNameValue;
            String[] initRoleCodes;
            // 获取类上的注解
            // handlerMethod.getBeanType() 用于获取实际处理请求的Controller类的类型
            Permission classAnnotation = handlerMethod.getBeanType().getAnnotation(Permission.class);

            // 优先获取Class上面Permission的groupName
            if (classAnnotation != null && classAnnotation.groupName() != null && !classAnnotation.groupName().trim()
                .isEmpty()) {
                groupNameValue = classAnnotation.groupName();
                initRoleCodes = classAnnotation.initRoleCodes();
            } else {
                // 如果Class的groupName为空, 则获取方法上面的GroupName
                if (methodAnnotation != null && methodAnnotation.groupName() != null && !methodAnnotation.groupName()
                    .trim().isEmpty()) {
                    groupNameValue = methodAnnotation.groupName();
                    initRoleCodes = methodAnnotation.initRoleCodes();
                } else {
                    // 如果Class和方法的groupName都为空, 那么设置当前Controller的ClassName为groupName
                    groupNameValue = handlerMethod.getBeanType().getSimpleName();
                    initRoleCodes = new String[]{};
                }
            }
            permissionDto.setInitRoleCodes(initRoleCodes);
            permissionDto.setGroupName(groupNameValue);
            urls.add(permissionDto);
        }
        return urls;
    }

    @GetMapping("/allUrls")
    public R<List<PermissionPageResp>> getAllUrls_() {
        Set<PermissionDto> urls = getAllUrls();
        List<PermissionPageResp> resps = new ArrayList<>();
        for (PermissionDto url : urls) {
            for (String s : url.getUrls()) {
                PermissionPageResp resp = new PermissionPageResp(s, url.isDefaultPass());
                resp.setGroupName(url.getGroupName());
                resps.add(resp);
            }
        }
        return R.ok(resps.stream().sorted(Comparator.comparing(PermissionPageResp::getId)).toList());
    }

    @Permission(defaultPass = true)
    @GetMapping("users")
    public R<PermissionDetailResp> getUsers(
        @RequestParam(name = "permissionId") String permissionId) {
        return R.ok(permissionService.getDetail(permissionId, true));
    }

    @GetMapping("role")
    @Permission(defaultPass = true)
    public R<PermissionDetailResp> getRoles(
        @RequestParam(name = "permissionId") String permissionId) {
        return R.ok(permissionService.getDetail(permissionId, false));
    }

    @PostMapping("/assign/users/{permissionId}")
    public R<Boolean> assignToUsers(
        @PathVariable String permissionId, @Valid @RequestBody PermissionUserAddReq req) {
        permissionService.assignToUsers(permissionId, req.getUserId());
        return R.ok(true);
    }

    @PostMapping("/assign/roles/{permissionId}")
    public R<Boolean> assignToRoles(
        @PathVariable String permissionId, @Valid @RequestBody PermissionRoleAddReq req) {
        permissionService.assignToRoles(permissionId, req.getRoleId());
        return R.ok(true);
    }

    @PostMapping("/add/users")
    public R<Boolean> addUsers(@Valid @RequestBody PermissionUserAddReq req) {
        if (CollectionUtils.isEmpty(req.getPermissionId())) {
            throw new BusinessException("权限列表不能为空");
        }
        ListUtil.splitAfterRun(
            999,
            req.getPermissionId(),
            spList -> {
                List<SysPermissionUserRef> refs =
                    new QSysPermissionUserRef()
                        .or()
                        .permission.groupName.eqIfPresent(req.getGroupName())
                        .permissionId
                        .in(spList)
                        .endOr()
                        .userId
                        .in(req.getUserId())
                        .findList();
                for (String s : req.getPermissionId()) {
                    for (String string : req.getUserId()) {
                        if (refs.stream()
                            .anyMatch(t -> t.getPermissionId().equals(s) && t.getUserId().equals(string))) {
                            continue;
                        }
                        new SysPermissionUserRef(new SysPermissionUserRefId(s, string)).save();
                    }
                }
            });
        return R.ok(true);
    }

    @PostMapping("/add/roles")
    public R<Boolean> addRoles(@Valid @RequestBody PermissionRoleAddReq req) {
        if (CollectionUtils.isEmpty(req.getPermissionId())) {
            throw new BusinessException("权限列表不能为空");
        }
        ListUtil.splitAfterRun(
            999,
            req.getPermissionId(),
            spList -> {
                List<SysPermissionRoleRef> refs =
                    new QSysPermissionRoleRef().or()
                        .permissionId
                        .in(spList)
                        .permission.groupName.eqOrNull(req.getGroupName())
                        .endOr()
                        .roleId
                        .in(req.getRoleId())
                        .findList();
                for (String s : spList) {
                    for (String string : req.getRoleId()) {
                        if (refs.stream()
                            .anyMatch(t -> t.getPermissionId().equals(s) && t.getRoleId().equals(string))) {
                            continue;
                        }
                        new SysPermissionRoleRef(new SysPermissionRoleRefId(s, string)).save();
                    }
                }
            });
        return R.ok(true);
    }

    /**
     * 移除
     */
    @GetMapping("remove/user")
    public R removeUser(@RequestParam(name = "userId") String userId) {
        new QSysPermissionUserRef().userId.eq(userId).delete();
        return R.ok();
    }

    /**
     * 移除
     */
    @GetMapping("remove/role")
    public R removeRole(@RequestParam(name = "roleId") String roleId) {
        new QSysPermissionRoleRef().roleId.in(roleId).delete();
        return R.ok();
    }
}
