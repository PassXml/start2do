package org.start2do.dto.permission;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.dto.Page;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class PermissionPageRequest extends Page<Object> {
    
    /**
     * URL匹配条件
     */
    private String url;
    
    /**
     * 是否允许通过
     */
    private Boolean pass;
}
