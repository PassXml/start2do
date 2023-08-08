package org.start2do.eventbus.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@ConfigurationProperties(prefix = "start2do.eventbus")
@AutoConfiguration
public class EventBusConfig {

    private boolean enabled = false;

    private Type type = Type.local;

    private String configUrl;

    public enum Type {
        local, Cluster
    }
}
