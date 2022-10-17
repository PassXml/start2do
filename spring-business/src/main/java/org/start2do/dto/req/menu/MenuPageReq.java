package org.start2do.dto.req.menu;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class MenuPageReq {

    private String name;

    public void setMenuName(String name) {
        this.name = name;
    }
}
