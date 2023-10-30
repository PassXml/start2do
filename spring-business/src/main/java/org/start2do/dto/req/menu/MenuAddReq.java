package org.start2do.dto.req.menu;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.ebean.dto.EnableType;
import org.start2do.ebean.enums.YesOrNoType;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class MenuAddReq {

    @NotEmpty
    @JsonAlias("menuName")
    private String name;
    @NotEmpty
    @JsonAlias("menuType")
    private String type;
    @NotEmpty
    @JsonAlias("routePath")
    private String path;
    private String icon;
    @JsonAlias("menuOrder")
    private Integer sort = 0;
    @JsonAlias("parentMenuId")
    private Integer parentId;
    @JsonAlias("menuPermission")
    private String permission;
    private YesOrNoType isShow;
    private EnableType status;


}
