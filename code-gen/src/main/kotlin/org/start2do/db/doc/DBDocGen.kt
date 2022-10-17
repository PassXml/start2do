package org.start2do.db.doc

import cn.smallbun.screw.core.Configuration
import cn.smallbun.screw.core.engine.EngineConfig
import cn.smallbun.screw.core.engine.EngineFileType
import cn.smallbun.screw.core.engine.EngineTemplateType
import cn.smallbun.screw.core.execute.DocumentationExecute
import cn.smallbun.screw.core.process.ProcessConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

class Config {
    lateinit var deviceClassName: String
    lateinit var jdbcUrl: String
    lateinit var username: String
    lateinit var password: String
    lateinit var outDir: String
    var fileType = EngineFileType.HTML
    var ignoreTables: List<String>? = null
    var ignorePrefix: String? = null
    var ignoreSuffix: String? = null
    var title: String? = null
    var version: String? = null
    var description: String? = null

    constructor()
    constructor(deviceClassName: String, jdbcUrl: String, username: String, password: String, outDir: String) {
        this.deviceClassName = deviceClassName;
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.outDir = outDir;
    }
}


object DBDocGen {

    fun run(config: Config) {
        //数据源
        val hikariConfig = HikariConfig()
        //        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.driverClassName = config.deviceClassName
        hikariConfig.jdbcUrl = config.jdbcUrl
        hikariConfig.username = config.username
        hikariConfig.password = config.password
        //设置可以获取tables remarks信息
        hikariConfig.addDataSourceProperty("useInformationSchema", "true")
        hikariConfig.minimumIdle = 2
        hikariConfig.maximumPoolSize = 5
        val dataSource: DataSource = HikariDataSource(hikariConfig)
        //生成配置
        val engineConfig = EngineConfig.builder() //生成文件路径
            .fileOutputDir(config.outDir) //打开目录
            .openOutputDir(true) //文件类型
            .fileType(config.fileType) //生成模板实现
            .produceType(EngineTemplateType.freemarker) //自定义文件名称
            .build()

        //忽略表
        val ignoreTableName = ArrayList<String>()
        config.ignoreTables?.let {
            ignoreTableName.addAll(it)
        }
        //忽略表前缀
        val ignorePrefix = ArrayList<String>()
        config.ignorePrefix?.let {
            ignorePrefix.add(it)
        }
        //忽略表后缀
        val ignoreSuffix = ArrayList<String>()
        config.ignorePrefix?.let {
            ignoreSuffix.add(it)
        }
        //
        val processConfig = ProcessConfig.builder() //指定生成逻辑、当存在指定表、指定表前缀、指定表后缀时，将生成指定表，其余表不生成、并跳过忽略表配置
            //根据名称指定表生成
            .designatedTableName(ArrayList()) //根据表前缀生成
            .designatedTablePrefix(ArrayList()) //根据表后缀生成
            .designatedTableSuffix(ArrayList()) //忽略表名
            .ignoreTableName(ignoreTableName) //忽略表前缀
            .ignoreTablePrefix(ignorePrefix) //忽略表后缀
            .ignoreTableSuffix(ignoreSuffix).build()
        //配置
        val configuration = Configuration.builder() //版本
            .version(config.version) //描述
            .description(config.description) //数据源
            .dataSource(dataSource) //生成配置
            .engineConfig(engineConfig) //生成配置
            .produceConfig(processConfig).build()
        //执行生成
        DocumentationExecute(configuration).execute()
    }
}