package org.start2do.dto.resp.dict;

import java.time.LocalDateTime;
import java.util.UUID;
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
public class DictPageResp {

    private UUID id;
    /**
     * createBy
     */
    private String createPerson;
    /**
     * createAt
     */
    private LocalDateTime createTime;
    /**
     * updateBy
     */
    private String updatePerson;
    /**
     * updateAt
     */
    private LocalDateTime updateTime;
    /**
     * dictName
     */
    private String dictName;
    /**
     * dictDesc
     */
    private String dictDesc;
    /**
     * dictType
     */
    private String dictType;
    private String dictTypeStr;
    /**
     * dictNote
     */
    private String dictNote;
}
