package org.start2do.dto.req.userauth;

import lombok.Data;
import lombok.experimental.Accessors;
import org.start2do.dto.Page;
import org.start2do.ebean.dto.EnableType;

@Data
@Accessors(chain = true)
public class UserAuthPageReq extends Page {
  // 过滤参数
  private String userId;
  private String username;
  private String realName;
  private String authType;
  private String authUsername;
  private String authUid;
  private EnableType status;

}
