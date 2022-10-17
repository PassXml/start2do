package org.start2do.entity.security;

import io.ebean.annotation.Cache;
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
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.ebean.entity.BaseModel2;

@Setter
@Getter
@Accessors(chain = true)
@Entity
@Table(name = "sys_role")
@Cache
public class SysRole extends BaseModel2 implements Serializable {

    @Id
    @Identity(start = 100)
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "name", length = 128)
    private String name;
    @Column(name = "role_code")
    private String roleCode;
    @Column(name = "descs")
    private String descs;
    @JoinTable(name = "sys_role_menu", joinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")},
        inverseJoinColumns = {@JoinColumn(name = "menu_id", referencedColumnName = "id")})
    @ManyToMany(fetch = FetchType.LAZY)
    private List<SysMenu> menus;

    @JoinTable(name = "sys_user_role", joinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")},
        inverseJoinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")})
    @ManyToMany
    private List<SysUser> users;
}
