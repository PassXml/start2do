package org.start2do.entity.security;

import io.ebean.Model;
import io.ebean.annotation.Cache;
import io.ebean.annotation.StorageEngine;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@Table(name = "sys_user_permission")
@Cache(enableQueryCache = true)
@IdClass(SysUserPermissionId.class)
@StorageEngine("ENGINE = MergeTree() order by id;")
public class SysUserPermission extends Model {

    @Id
    @Column(name = "menu_id")
    private Integer menuId;
    @Id
    @Column(name = "user_id")
    private Integer userId;

}
