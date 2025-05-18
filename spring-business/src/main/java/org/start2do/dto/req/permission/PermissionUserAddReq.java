package org.start2do.dto.req.permission;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class PermissionUserAddReq {
  @JsonAlias("userIds")
  @Size(max = 999)
  private List<String> userId;

  private List<String> permissionId;
}
