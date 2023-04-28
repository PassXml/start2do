package org.start2do.dto.req.user;

import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Data
@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class UserAddReq {

    /**
     * username
     */
    @NotEmpty
    private String username;
    /**
     * actualName
     */
    private String realName;
    /**
     * status
     */
    @NotEmpty
    private String status;
    /**
     * phone
     */
    private String phone;
    /**
     * email
     */
    private String email;
    private String avatar;
    /**
     * password
     */

    private String password;
    @NotNull
    private Integer deptId;
    @NotEmpty
    private List<Integer> roles;
}
