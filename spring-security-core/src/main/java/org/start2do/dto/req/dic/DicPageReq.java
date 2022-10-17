package org.start2do.dto.req.dic;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.dto.Page;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class DicPageReq extends Page {

    private String paramName;
    private String type;
}
