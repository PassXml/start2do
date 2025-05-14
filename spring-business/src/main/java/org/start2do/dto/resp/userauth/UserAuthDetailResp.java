package org.start2do.dto.resp.userauth;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;
import org.start2do.constant.Constant;
import org.start2do.ebean.dto.EnableType;
import org.start2do.util.DictUtil;

@Data
@Accessors(chain = true)
public class UserAuthDetailResp {
  private String id;
  private String userId;
  private String authType;
  private String authUid;
  private String authUsername;
  private String authToken; // 详情中可以包含 token
  private EnableType status;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;

  public String getAuthType() {
    return DictUtil.getLabel(Constant.TYPE_USER_AUTH, authType);
  }
}
