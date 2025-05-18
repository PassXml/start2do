package org.start2do.dto.resp.dept;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.entity.security.SysUserDept.Type;
import org.start2do.util.EnumUtil;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class DeptUsersResp {

  private String id;
  private String name;

  /** 姓名 */
  private String realName;

  /** 是否主部门 */
  private Type type;

  public DeptUsersResp(String id, String name, String realName) {
    this.id = id;
    this.name = name;
    this.realName = realName;
  }

  public String getTypeStr() {
    return EnumUtil.toStr(type);
  }
}
