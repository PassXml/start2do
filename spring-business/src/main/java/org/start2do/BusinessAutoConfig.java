package org.start2do;


import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;

@Import(BusinessConfig.class)
@ConditionalOnProperty(prefix = "start2do.business", value = "enable", havingValue = "true")
public class BusinessAutoConfig {

}
