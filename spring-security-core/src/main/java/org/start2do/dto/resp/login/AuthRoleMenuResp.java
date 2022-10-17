package org.start2do.dto.resp.login;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.entity.security.SysMenu;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class AuthRoleMenuResp {

    private Integer id;
    private String name;
    private String path;
    private String permission;
    private String icon;
    private String sort;
    private Integer parentId;
    private String type;
    private String typeStr;

    public AuthRoleMenuResp(SysMenu menu) {
        this.id = menu.getId();
        this.name = menu.getName();
        this.path = menu.getPath();
        this.permission = menu.getPermission();
        this.icon = menu.getIcon();
        this.sort = menu.getSort();
        this.parentId = menu.getParentId();
        this.type = menu.getType().getValue();
        this.typeStr = menu.getType().getLabel();
    }
}
