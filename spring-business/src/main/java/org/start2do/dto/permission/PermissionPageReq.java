package org.start2do.dto.permission;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.dto.Page;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PermissionPageReq extends Page {

    private String url;
    private Boolean pass;
}
