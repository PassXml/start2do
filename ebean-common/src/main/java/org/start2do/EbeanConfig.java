package org.start2do;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@NoArgsConstructor
@ConfigurationProperties(prefix = "start2do.ebean")
public class EbeanConfig {

    private boolean migration = false;
    private boolean multipleDataSources = false;
    private boolean enable = false;
}
