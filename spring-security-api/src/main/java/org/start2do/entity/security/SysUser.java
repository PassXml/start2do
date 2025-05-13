package org.start2do.entity.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.annotation.Cache;
import io.ebean.annotation.DbComment;
import io.ebean.annotation.DbDefault;
import io.ebean.annotation.DbForeignKey;
import io.ebean.annotation.StorageEngine;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.dto.BusinessException;
import org.start2do.ebean.dict.IDictItem;
import org.start2do.ebean.entity.BaseModel2;
import org.start2do.ebean.id_generators.SnowflakeStrGenerator;
import org.start2do.entity.security.SysUserDept.Type;
import org.start2do.entity.security.query.QSysDept;
import org.start2do.entity.security.query.QSysUserDept;

@Setter
@Getter
@Accessors(chain = true)
@Entity
@Table(name = "sys_user")
@NoArgsConstructor
@Cache(enableQueryCache = true)
@StorageEngine("ENGINE = MergeTree() order by id;")
public class SysUser extends BaseModel2 implements Serializable {

  @Id
  @GeneratedValue(generator = SnowflakeStrGenerator.KEY)
  private String id;

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

  @DbComment("密码过期时间")
  @Column(name = "pw_expiration_time")
  private LocalDateTime pwExpirationTime;

  @JoinTable(
      name = "sys_user_dept",
      joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
      inverseJoinColumns = {@JoinColumn(name = "dept_id", referencedColumnName = "id")})
  @JsonIgnore
  @ManyToMany(fetch = FetchType.LAZY)
  @DbForeignKey(noConstraint = true)
  private List<SysDept> dept;

  public SysDept getMainDept() {
    return new QSysDept()
        .id
        .eq(
            new QSysUserDept()
                .select(QSysUserDept.alias().deptId)
                .userId
                .eq(this.id)
                .type
                .eq(Type.Main)
                .query())
        .findOne();
  }

  @JoinTable(
      name = "sys_user_role",
      joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
      inverseJoinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")})
  @ManyToMany(fetch = FetchType.LAZY)
  @JsonIgnore
  private List<SysRole> roles;

  @JoinTable(
      name = "sys_user_permission",
      joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
      inverseJoinColumns = {@JoinColumn(name = "menu_id", referencedColumnName = "id")})
  @ManyToMany(fetch = FetchType.LAZY)
  @JsonIgnore
  private List<SysMenu> menus;

  public SysUser(
      String username,
      String realName,
      String password,
      String email,
      String phone,
      LocalDateTime pwExpirationTime,
      List<SysRole> roles,
      List<SysMenu> menus) {
    this.username = username;
    this.realName = realName;
    this.password = password;
    this.email = email;
    this.phone = phone;
    this.roles = roles;
    this.menus = menus;
    this.pwExpirationTime = pwExpirationTime;
    this.status = Status.Normal;
  }

  public enum Status implements IDictItem {
    Normal("1", "正常"),
    Lock("0", "锁定");

    Status(String value, String label) {
      putItemBean(value, label);
    }

    @JsonCreator
    public static Status find(String s) {
      for (Status value : values()) {
        if (value.getValue().equals(s)) {
          return value;
        }
      }
      throw new BusinessException(String.format("%s未知字典值:%s", "Status", s));
    }
  }

  @PrePersist
  @PreUpdate
  public void initPwExpirationTime() {
    if (pwExpirationTime == null) {
      this.pwExpirationTime = LocalDateTime.now().plusYears(2);
    }
  }
}
