package org.start2do.dto.req.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class UpdateCurrentUserInfoDto {

    @NotBlank(message = "用户ID不能为空")
    private String id;

    private String realName;
    private String userPhone;
    private String userEmail;
    private String avatar;
    private String enterpriseWechat;
}
