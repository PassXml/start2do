package org.start2do;


import javax.sql.DataSource;
import org.apache.ibatis.io.VFS;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.start2do.typehandle.AutoGenericEnumTypeHandler;
import org.start2do.typehandle.UUIDTypeHandler;

@AutoConfiguration
@Order(Ordered.HIGHEST_PRECEDENCE)
@Import(MultiDatasourcePrimaryConfig.class)
@ConditionalOnProperty(prefix = "spring.datasource", name = "url")
@AutoConfigureBefore(MybatisAutoConfiguration.class)
@ConditionalOnMissingBean(MybatisAutoConfiguration.class)
public class MultiDatasourcePrimaryAutoConfig {


    public final static String MainSqlSessionFactory = "MainSqlSessionFactory";

//    @Primary
//    @Bean("MainDataSource")
//    public DataSource dataSource(MultiDatasourcePrimaryConfig config) {
//        return DataSourceBuilder.create().url(config.getUrl()).driverClassName(config.getDriverClassName())
//            .type(config.getType()).username(config.getUsername()).password(config.getPassword()).build();
//    }

    @Primary
    @Bean("MainSqlSessionFactory")
    public SqlSessionFactory mainSqlSessionFactory(DataSource dataSource)
        throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        VFS.addImplClass(SpringBootVFS.class);
        bean.setDefaultEnumTypeHandler(AutoGenericEnumTypeHandler.class);
        bean.setTypeHandlers(new UUIDTypeHandler());
        bean.setDataSource(dataSource);
        // mapper的xml形式文件位置必须要配置，不然将报错：no statement （这种错误也可能是mapper的xml中，namespace与项目的路径不一致导致）
        bean.setMapperLocations(
            new PathMatchingResourcePatternResolver().getResources("classpath*:mybatis/mapper/*.xml"));
        bean.setConfigLocation(
            new PathMatchingResourcePatternResolver().getResource("classpath:mybatis/mybatis-config.xml"));
        return bean.getObject();
    }


    @Primary
    @Bean("MainSqlSessionTemplate")
    public SqlSessionTemplate mainSqlSessionTemplate(
        @Qualifier("MainSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
