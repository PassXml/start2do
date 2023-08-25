package org.start2do;

import lombok.RequiredArgsConstructor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("org.start2do")
@RequiredArgsConstructor
@MapperScan(basePackages = "org.start2do.mapper", sqlSessionFactoryRef = EbeanClickHouseAutoConfig.ClickHouseSessionFactory)
public class AutoScanConfig {

}
