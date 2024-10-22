package org.start2do.entity.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.ebean.annotation.Cache;
import io.ebean.annotation.DbComment;
import io.ebean.annotation.Identity;
import io.ebean.annotation.IdentityType;
import io.ebean.annotation.StorageEngine;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.dto.BusinessException;
import org.start2do.ebean.dict.IDictItem;
import org.start2do.ebean.dto.EnableType;
import org.start2do.ebean.entity.BaseModel2;
import org.start2do.ebean.enums.YesOrNoType;

@Setter
@Getter
@Accessors(chain = true)
@Entity
@Table(name = "sys_menu")
@NoArgsConstructor
@Cache(enableQueryCache = true)
@StorageEngine("ENGINE = MergeTree() order by id;")
public class SysMenu extends BaseModel2 implements Serializable {

    @Id
    @Identity(start = 100, type = IdentityType.IDENTITY)
    private Integer id;
    @Column(name = "name", length = 128)
    private String name;
    @Column(name = "path")
    private String path;
    @Column(name = "permission", length = 128)
    private String permission;
    @Column(name = "icon")
    private String icon;
    @Column(name = "sort")
    private String sort;
    @Column(name = "parent_id")
    private Integer parentId;
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private SysMenu parent;
    @Column
    @DbComment("类型")
    private Type type;
    @DbComment("是否显示")
    private YesOrNoType isShow;
    @DbComment("是否启用")
    private EnableType status;


    @JoinTable(name = "sys_role_menu", joinColumns = {@JoinColumn(name = "menu_id", referencedColumnName = "id")},
        inverseJoinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")})
    @ManyToMany(fetch = FetchType.LAZY)
    private List<SysRole> roles;

    public enum Type implements IDictItem {
        Directory("0", "目录"),
        Menu("1", "菜单"),
        Button("2", "按钮"),
        Link("3", "链接"),
        Iframe("4", "内嵌"),
        ;

        Type(String value, String label) {
            putItemBean(value, label);
        }

        @JsonCreator
        public static Type find(String s) {
            for (Type value : values()) {
                if (value.getValue().equals(s)) {
                    return value;
                }
            }
            throw new BusinessException(String.format("%s未知字典值:%s", "Type", s));
        }
    }

    @PrePersist
    public void prePersist() {
        if (isShow == null) {
            isShow = YesOrNoType.No;
        }
        if (status == null) {
            status = EnableType.DisEnable;
        }
    }
}
