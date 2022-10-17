package org.start2do.dto.resp.dic;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class DicPageResp {

    private Integer id;
    private String name;
    private String paramCode;
    private String paramKey;
    private String paramName;
    private String paramStatus;
    private String paramType;
    private String paramValue;
    private LocalDateTime createTime;
    private String createPerson;
    private LocalDateTime updateTime;
    private String updatePerson;

}
