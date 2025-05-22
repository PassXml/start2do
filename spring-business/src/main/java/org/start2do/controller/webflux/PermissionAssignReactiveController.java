package org.start2do.controller.webflux;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.dto.Permission;
import org.start2do.dto.R;
import org.start2do.dto.permission.PermissionDetailResp;
import org.start2do.service.IPermissionService;
import reactor.core.publisher.Mono;
import org.start2do.dto.Permission;

@RestController
@RequestMapping("/permission")
@ConditionalOnWebApplication(type = Type.REACTIVE)
@RequiredArgsConstructor
@Permission(groupName = "权限分配管理")
public class PermissionAssignReactiveController {

  private final IPermissionService permissionService;

//  @GetMapping("/allUrls")
//  public R<List<PermissionPageResp>> getAllUrls_() {
//    List<PermissionPageResp> resps = new ArrayList<>();
//    for (PermissionDto url : urls) {
//      for (String s : url.getUrls()) {
//        resps.add(new PermissionPageResp(s, url.isDefaultPass()));
//      }
//    }
//    return R.ok(resps.stream().sorted(Comparator.comparing(PermissionPageResp::getId)).toList());
//  }

  @Permission(defaultPass = true)
  @GetMapping("users")
  public R<PermissionDetailResp> getUsers(@RequestParam("permissionId") String permissionId) {
    return R.ok(permissionService.getDetail(permissionId, true));
  }

  @GetMapping("role")
  @Permission(defaultPass = true)
  public R<PermissionDetailResp> getRoles(@RequestParam("permissionId") String permissionId) {
    return R.ok(permissionService.getDetail(permissionId, false));
  }

  @PostMapping("/assign/users/{permissionId}")
  public Mono<R<Boolean>> assignToUsers(
      @PathVariable String permissionId, @RequestBody List<String> userIds) {
    boolean result = permissionService.assignToUsers(permissionId, userIds);
    return Mono.just(R.ok(result));
  }

  @PostMapping("/assign/roles/{permissionId}")
  public Mono<R<Boolean>> assignToRoles(
      @PathVariable String permissionId, @RequestBody List<String> roleIds) {
    boolean result = permissionService.assignToRoles(permissionId, roleIds);
    return Mono.just(R.ok(result));
  }

}
