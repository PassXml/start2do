package org.start2do.spring

import lombok.SneakyThrows
import org.jboss.forge.roaster.Roaster
import org.jboss.forge.roaster.model.source.JavaClassSource
import org.start2do.FileUtil
import java.io.FileInputStream
import java.nio.file.Paths

class Config {
    lateinit var entityDir: String
    lateinit var entityFiles: List<String>
    lateinit var outDir: String
    val suffix = "Controller"

    constructor(entityDir: String, outDir: String) {
        this.entityDir = entityDir
        this.entityFiles = FileUtil.getFiles(
            Paths.get(entityDir).toFile()
        ) { s: String -> s.endsWith(".java") }
        this.outDir = outDir
    }
}

object ControllerGen {

    @SneakyThrows
    fun run(config: Config) {
        for (entityFile in config.entityFiles) {
            val file = Paths.get(config.outDir + "/" + entityFile + config.suffix + ".java").toFile()
            var source: JavaClassSource
            source = if (file.exists()) {
                Roaster.parse(JavaClassSource::class.java, FileInputStream(file))
            } else {
                Roaster.create(JavaClassSource::class.java)
            }
            addDoc(source)
        }
//        Roaster.
    }

    private fun addDoc(source: JavaClassSource) {
        val doc = source.javaDoc
//        doc.getTags("@an");
    }
}