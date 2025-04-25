package org.start2do.dto.resp.dept;

import java.util.List;
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
public class DeptTreeResp {

    private Integer id;
    private Integer parentId;
    private Integer sort;
    private String name;
    private List<DeptTreeResp> children;
}
