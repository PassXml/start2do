package org.start2do.dto.resp.file;

import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class FilePageResp {

    private String id;
    private String name;
    private String md5;
    private Long size;
    private String host;
    private String path;
    private String url;
    private String createPerson;
    private LocalDate createTime;
}
