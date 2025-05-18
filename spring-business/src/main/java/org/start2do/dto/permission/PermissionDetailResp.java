package org.start2do.dto.permission;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PermissionDetailResp {

    /**
     * 权限ID
     */
    private String id;

    /**
     * 权限URL
     */
    private String url;

    /**
     * 是否允许通过
     */
    private boolean pass;

    /**
     * 关联用户列表
     */
    private List<UserDto> users;

    /**
     * 关联角色列表
     */
    private List<RoleDto> roles;

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class UserDto {
        /**
         * 用户ID
         */
        private String id;

        /**
         * 用户名
         */
        private String username;
    }

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class RoleDto {
        /**
         * 角色ID
         */
        private String id;

        /**
         * 角色名称
         */
        private String roleName;
    }
}
