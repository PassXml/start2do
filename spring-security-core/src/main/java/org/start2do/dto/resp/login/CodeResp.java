package org.start2do.dto.resp.login;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class CodeResp {

    private String key;
    private String image;

    public CodeResp(String key, String image) {
        this.key = key;
        this.image = image;
    }
}
