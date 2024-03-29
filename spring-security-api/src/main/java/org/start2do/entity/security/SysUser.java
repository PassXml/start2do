package org.start2do.entity.security;

import io.ebean.annotation.Cache;
import io.ebean.annotation.DbDefault;
import io.ebean.annotation.DbEnumValue;
import io.ebean.annotation.Identity;
import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.dto.BusinessException;
import org.start2do.ebean.entity.BaseModel2;

@Setter
@Getter
@Accessors(chain = true)
@Entity
@Table(name = "sys_user")
@NoArgsConstructor
@Cache
public class SysUser extends BaseModel2 implements Serializable {

    @Id
    @Identity(start = 100)
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "username", length = 128)
    private String username;
    @Column(name = "real_name", length = 32)
    private String realName;
    @Column(name = "password", length = 128)
    private String password;
    @Column(name = "status")
    @DbDefault("1")
    private Status status;
    @Column(name = "email", length = 128)
    private String email;
    @Column(name = "avatar", length = 2048)
    private String avatar;
    @Column(name = "phone", length = 32)
    private String phone;
    @Column(name = "dept_id")
    private Integer deptId;
    @JoinColumn(name = "dept_id", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private SysDept dept;
    @JoinTable(
        name = "sys_user_role",
        joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
        inverseJoinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")}
    )
    @ManyToMany(fetch = FetchType.LAZY)
    private List<SysRole> roles;
    @JoinTable(
        name = "sys_user_permission",
        joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
        inverseJoinColumns = {@JoinColumn(name = "menu_id", referencedColumnName = "id")}
    )
    @ManyToMany(fetch = FetchType.LAZY)
    private List<SysMenu> menus;

    public SysUser(String username, String realName, String password, String email, String phone, Integer deptId,
        List<SysRole> roles, List<SysMenu> menus) {
        this.username = username;
        this.realName = realName;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.deptId = deptId;
        this.roles = roles;
        this.menus = menus;
    }

    public enum Status {
        Normal("1", "正常"), Lock("0", "锁定");
        private String value;
        private String label;

        Status(String value, String label) {
            this.value = value;
            this.label = label;
        }

        @DbEnumValue
        public String getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }

        public static Status find(String s) {
            for (Status value : values()) {
                if (value.getValue().equals(s)) {
                    return value;
                }
            }
            throw new BusinessException(String.format("%s未知字典值:%s", "Status", s));
        }
    }
}
