package org.start2do.dto.resp.user;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class UserDetailResp {

    private String id;
    private String username;
    private String realName;
    private String status;
    private String statusStr;
    private String phone;
    private String email;
    private String avatar;
    /**
     * 主部门
     */
    private String deptId;
    private String deptName;
    private List<String> menus;
    private List<String> roles;
    private List<Item> rolesInfo;
    private List<Item> depts;

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class Item {

        private String id;
        private String name;

        public Item(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
