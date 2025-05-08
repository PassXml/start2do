package org.start2do.entity.security;

import io.ebean.Model;
import io.ebean.annotation.Cache;
import io.ebean.annotation.StorageEngine;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.constant.DBConstant;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@Table(name = "sys_user_menu")
@Cache(enableQueryCache = true)
@StorageEngine("ENGINE = MergeTree() order by id;")
public class SysUserMenu extends Model {

    @Id
    @EmbeddedId
    private SysUserMenuId id;
    @Column(name = "menu_id", length = DBConstant.ID_STR_LENGTH)
    private String menuId;
    @Column(name = "user_id", length = DBConstant.ID_STR_LENGTH)
    private String userId;
}
