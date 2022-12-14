package org.start2do.entity.security;

import io.ebean.annotation.Cache;
import io.ebean.annotation.DbComment;
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
@Table(name = "sys_menu")
@NoArgsConstructor
@Cache
public class SysMenu extends BaseModel2 implements Serializable {

    @Id
    @Identity(start = 100)
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
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


    @JoinTable(name = "sys_role_menu", joinColumns = {@JoinColumn(name = "menu_id", referencedColumnName = "id")},
        inverseJoinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")})
    @ManyToMany(fetch = FetchType.LAZY)
    private List<SysRole> roles;

    public enum Type {
        Menu("0", "普通路由(显示)"),
        HideMenu("1", "隐藏路由"),
        Button("2", "按钮"),
        ;
        private String value;
        private String label;

        Type(String value, String label) {
            this.value = value;
            this.label = label;
        }

        @DbEnumValue(length = 2)
        public String getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }

        public static Type find(String s) {
            for (Type value : values()) {
                if (value.getValue().equals(s)) {
                    return value;
                }
            }
            throw new BusinessException(String.format("%s未知字典值:%s", "Type", s));
        }
    }
}
