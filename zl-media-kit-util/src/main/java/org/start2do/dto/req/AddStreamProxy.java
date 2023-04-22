package org.start2do.dto.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class AddStreamProxy {

    @JsonIgnore
    public final static String URL = "/index/api/addStreamProxy";
    private String app;
    private String stream;
    private String url;

    public AddStreamProxy(String app, String stream, String url) {
        this.app = app;
        this.stream = stream;
        this.url = url;
    }
}
