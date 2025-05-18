package org.start2do.dto.resp.permission;

import lombok.Getter;
import lombok.Setter;
import org.start2do.util.Md5Util;

@Setter
@Getter
public class PermissionPageResp {
  private String id;
  private String url;
  private boolean defaultPass;

  public PermissionPageResp(String id, String url, boolean defaultPass) {
    this.id = id;
    this.url = url;
    this.defaultPass = defaultPass;
  }

  public PermissionPageResp(String url, boolean defaultPass) {
    this.id = Md5Util.md5(url);
    this.url = url;
    this.defaultPass = defaultPass;
  }
}
