package org.start2do.entity.security;

import io.ebean.annotation.DbComment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.constant.DBConstant;
import org.start2do.ebean.dto.EnableType;
import org.start2do.ebean.entity.BaseModel2;
import org.start2do.ebean.id_generators.SnowflakeStrGenerator;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@Table(name = "sys_user_auth")
@DbComment("第三方登录信息表")
public class SysUserAuth extends BaseModel2 {

  @Id
  @GeneratedValue(generator = SnowflakeStrGenerator.KEY)
  private String id;

  @Column(name = "user_id", nullable = false, length = DBConstant.ID_STR_LENGTH)
  private String userId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  private SysUser user;

  @DbComment("认证类型")
  @Column(name = "auth_type", length = 32, nullable = false)
  private String authType;

  @DbComment("第三方唯一标识")
  @Column(name = "auth_uid", length = 128, nullable = false)
  private String authUid;

  @DbComment("第三方用户名")
  @Column(name = "auth_username", length = 128)
  private String authUsername;

  @DbComment("可选，存储token等")
  @Column(name = "auth_token", length = 2048)
  private String authToken;

  @DbComment("1正常，0禁用")
  @Column(name = "status")
  private EnableType status = EnableType.Enable;

  public SysUserAuth(
      String userId, String authType, String authUid, String authUsername, String authToken) {
    this.userId = userId;
    this.authType = authType;
    this.authUid = authUid;
    this.authUsername = authUsername;
    this.authToken = authToken;
    this.status = EnableType.Enable;
  }
}
