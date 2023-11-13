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

    private Integer id;
    private String username;
    private String realName;
    private String status;
    private String statusStr;
    private String phone;
    private String email;
    private String avatar;
    private String deptId;
    private String deptName;
    private List<Integer> menus;
    private List<Integer> roles;
    private List<Item> rolesInfo;
    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class Item{
        private Integer id;
        private String name;

        public Item(Integer id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
