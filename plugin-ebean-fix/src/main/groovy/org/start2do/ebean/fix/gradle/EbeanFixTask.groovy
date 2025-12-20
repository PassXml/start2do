package org.start2do.ebean.fix.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.start2do.ebean.fix.QueryBeanGradlePlugin

/**
 * Ebean QueryBean 转换任务
 */
abstract class EbeanFixTask extends DefaultTask {

    @InputDirectory
    abstract DirectoryProperty getClassesDir()

    EbeanFixTask() {
        group = 'build'
        description = '转换 Ebean QueryBean 字段访问为方法调用'
    }

    @TaskAction
    void transform() {
        def dir = classesDir.get().asFile
        if (dir.exists() && dir.isDirectory()) {
            println "[Ebean Fix Task] 开始转换: ${dir.absolutePath}"
            QueryBeanGradlePlugin.transformBuildOutput(dir)
        } else {
            println "[Ebean Fix Task] 目录不存在，跳过转换: ${dir.absolutePath}"
        }
    }
}
