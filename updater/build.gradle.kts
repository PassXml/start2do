plugins {
    id("java")
    id("application")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "org.start2do.jar"

repositories {
    mavenCentral()
}
dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.apache.commons:commons-compress:1.27.1")
}

tasks.test {
    useJUnitPlatform()
}
application {
    mainClass.set("org.start2do.jar.JarUpdater")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")
}
tasks {
    // 配置 ShadowJar 任务
    shadowJar {
        // 设置生成的 JAR 文件名称
        archiveFileName.set("${project.name}-${project.version}-all.jar")
        // 合并服务文件（如果需要）
        mergeServiceFiles()

        // 排除签名文件（可选）
        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")

        // 可选：设置主类（如果需要运行 JAR 文件）
        manifest {
            attributes["Main-Class"] = "org.start2do.jar.JarUpdater"
        }
    }

    // 确保 build 任务依赖于 shadowJar 任务
    build {
        dependsOn(shadowJar)
    }
}
