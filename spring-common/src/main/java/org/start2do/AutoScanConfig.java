package org.start2do;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ComponentScan("org.start2do")
@Import(SpringCommonConfig.class)
public class AutoScanConfig {

}
