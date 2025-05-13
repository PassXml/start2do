package org.start2do.dto.resp.menu;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class MenuDetailResp {

    private String id;

    private String name;
    private String type;
    private String typeStr;
    private String icon;
    private Integer sort;
    private String permission;
    private String parentId;
    private String path;


    private String createPerson;
    private LocalDateTime createTime;
    private String updatePerson;
    private LocalDateTime updateTime;
    private String status;
    private String statusStr;
    private String isShow;
    private String isShowStr;


}
