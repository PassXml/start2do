package org.start2do.ops.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.context.annotation.Import;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Import(OpsConfig.class)
public class AutoConfig {

}
