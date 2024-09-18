package org.start2do.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class MenuRespV2 {

    private String label;
    private Object value;

    private Object value2;

    private Short sort;

    public MenuRespV2(String label, Object value, Object value2) {
        this.label = label;
        this.value = value;
        this.value2 = value2;
    }
}
