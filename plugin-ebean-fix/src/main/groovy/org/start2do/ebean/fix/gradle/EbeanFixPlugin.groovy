package org.start2do.ebean.fix.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

/**
 * Ebean QueryBean 编译时转换插件
 * 在 Java 编译完成后自动将 QueryBean 属性访问转换为方法调用
 */
class EbeanFixPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println "[Ebean Fix Plugin] 已应用到项目: ${project.name}"

        // 创建配置扩展
        def extension = project.extensions.create('ebeanFix', EbeanFixExtension)

        // 延迟到 afterEvaluate 阶段注册任务，避免配置阶段冲突
        project.afterEvaluate {
            // 先收集所有 JavaCompile 任务到列表，避免并发修改异常
            def compileTasks = project.tasks.withType(JavaCompile).toList()

            // 为每个 JavaCompile 任务创建对应的转换任务
            compileTasks.each { JavaCompile compileTask ->
                // 跳过没有源文件的编译任务（如 compileTestJava 但项目无测试代码）
                if (compileTask.source == null || compileTask.source.isEmpty()) {
                    if (extension.verbose) {
                        println "[Ebean Fix Plugin] 跳过 ${compileTask.name}：无源文件"
                    }
                    return
                }

                // 创建转换任务
                def taskName = "ebeanFix${compileTask.name.capitalize()}"

                def transformTask = project.tasks.create(taskName, EbeanFixTask) {
                    classesDir.set(compileTask.destinationDirectory)
                    enabled = extension.enabled
                    onlyIf { extension.enabled }
                }

                // 让转换任务在编译任务后执行
                compileTask.finalizedBy(transformTask)

                // 配置任务依赖
                transformTask.mustRunAfter(compileTask)
            }

            // 添加一个统一的转换任务（可选）
            if (!compileTasks.isEmpty()) {
                project.tasks.create('ebeanFixAll') {
                    group = 'build'
                    description = '转换所有编译输出的 Ebean QueryBean'
                    dependsOn project.tasks.withType(EbeanFixTask)
                }
            }
        }
    }
}
