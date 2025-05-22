package org.start2do.dto.resp.user;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class CurrentUserInfoDto {

    private String id;
    private String name;
    private String realName;
    private String userPhone;
    private String userEmail;
    private String avatar;
    private String deptId;
    private String deptName;
    private List<String> deptNames;
    private String enterpriseWechat;
}
