package org.start2do.dto.resp.file;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class SysFileUploadResp {

    private String id;
    private String relativeFilePath;
    private String url;

    public SysFileUploadResp(String id, String relativeFilePath) {
        this.id = id;
        this.relativeFilePath=relativeFilePath;
    }

    public SysFileUploadResp(String id, String relativeFilePath, String url) {
        this.id = id;
        this.relativeFilePath = relativeFilePath;
        this.url = url;
    }
}
