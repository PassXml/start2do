package org.start2do.dto.resp.setting;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.ebean.enums.YesOrNoType;
import org.start2do.util.EnumUtil;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class SettingPageResp {

  private Integer id;
  private String enable;
  private String enableStr;
  private String remark;
  private String type;
  private String key;
  private String value;
  private Integer sort;
  private YesOrNoType isBuiltIn;

  public String getIsBuiltInStr() {
    return EnumUtil.toStr(isBuiltIn);
  }
}
