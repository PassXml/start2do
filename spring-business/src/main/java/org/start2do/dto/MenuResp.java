package org.start2do.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class MenuResp {

    private String label;
    private Object value;

    public MenuResp(String label, Object value) {
        this.label = label;
        this.value = value;
    }
}
