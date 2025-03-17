package org.start2do;


import java.util.Arrays;
import javax.sql.DataSource;
import lombok.experimental.UtilityClass;
import org.apache.ibatis.io.VFS;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.start2do.typehandle.AutoGenericEnumTypeHandler;
import org.start2do.typehandle.UUIDTypeHandler;

@UtilityClass
public class MybatisDatasourceFactory {


    public DataSource dataSource(MultiDatasourcePrimaryConfig config) {
        return DataSourceBuilder.create().url(config.getUrl()).driverClassName(config.getDriverClassName())
            .type(config.getType()).username(config.getUsername()).password(config.getPassword()).build();
    }

    public SqlSessionFactory sqlSessionFactory(String dataType, DataSource dataSource,
        Resource[] mapperLocations, Resource configLocation)
        throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        VFS.addImplClass(SpringBootVFS.class);
        try {
            Class<?> aClass = Class.forName("org.start2do.ebean.dict.IDictItem");
            bean.setDefaultEnumTypeHandler(AutoGenericEnumTypeHandler.class);
        } catch (Exception e) {
        }
        bean.setTypeHandlers(new UUIDTypeHandler());
        bean.setDataSource(dataSource);
        // mapper的xml形式文件位置必须要配置，不然将报错：no statement （这种错误也可能是mapper的xml中，namespace与项目的路径不一致导致）
        if (mapperLocations != null) {
            bean.setMapperLocations(mapperLocations);
        } else {
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(
                "classpath*:mybatis/mapper/*.xml");
            String t;
            if (dataType != null && !dataType.isEmpty()) {
                t = "_" + dataType + ".xml";
            } else {
                t = null;
            }
            bean.setMapperLocations(Arrays.stream(resources).filter(resource -> {
                if (t == null) {
                    return true;
                }
                return resource.getFilename() != null && resource.getFilename().endsWith(t);
            }).toArray(Resource[]::new));
        }
        if (configLocation != null) {
            bean.setConfigLocation(configLocation);
        } else {
            bean.setConfigLocation(
                new PathMatchingResourcePatternResolver().getResource("classpath:mybatis/mybatis-config.xml"));
        }
        return bean.getObject();
    }


    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    public MapperScannerConfigurer mapperScannerConfigurer(String sqlSessionFactoryBeanName, String basePackage) {
        MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
        mapperScannerConfigurer.setSqlSessionFactoryBeanName(sqlSessionFactoryBeanName);
        mapperScannerConfigurer.setBasePackage(basePackage);
        return mapperScannerConfigurer;
    }
}
