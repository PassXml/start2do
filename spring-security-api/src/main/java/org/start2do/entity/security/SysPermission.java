package org.start2do.entity.security;

import io.ebean.Model;
import io.ebean.annotation.DbComment;
import io.ebean.annotation.Identity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@DbComment("权限表")
@Table(name = "sys_permission")
public class SysPermission extends Model {

    @Id
    @Identity
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(length = 4096)
    private String url;
    @JoinTable(
        name = "sys_permission_role_ref",
        joinColumns = {@JoinColumn(name = "permission_id", referencedColumnName = "id")},
        inverseJoinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")}
    )
    @ManyToMany(fetch = FetchType.LAZY)
    public List<SysRole> roles;
    @JoinTable(
        name = "sys_permission_user_ref",
        joinColumns = {@JoinColumn(name = "permission_id", referencedColumnName = "id")},
        inverseJoinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")}
    )
    @ManyToMany(fetch = FetchType.LAZY)
    private List<SysUser> users;
}
