package org.start2do.enums

import io.ebean.annotation.DbEnumValue
import org.jboss.forge.roaster.ParserException
import org.jboss.forge.roaster.Roaster
import org.jboss.forge.roaster.model.impl.JavaEnumImpl
import org.jboss.forge.roaster.model.source.JavaClassSource
import org.jboss.forge.roaster.model.source.JavaDocSource
import org.start2do.util.FileUtil
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

class Config {
    lateinit var files: List<String>
    lateinit var outFile: String
    var packageName: String? = null
    var name = "TypeDtoMapper"
    var force = false

    constructor(filePath: String?, outFile: String) {
        files = FileUtil.getFiles(
            Paths.get(filePath).toFile()
        ) { s: String -> s.endsWith(".java") }
        this.outFile = outFile
    }

    constructor(filePath: String?, outFile: String, packageName: String?) {
        files = FileUtil.getFiles(
            Paths.get(filePath).toFile()
        ) { s: String -> s.endsWith(".java") }
        this.outFile = outFile
        this.packageName = packageName
    }
}

class TypeInfoPojo(val typeName: String, val packagePath: String)
object EnumGen {


    fun run(config: Config) {
        val hasAnnotations: MutableList<TypeInfoPojo> = ArrayList()
        for (file in config.files) {
            try {
                val source = Roaster.parse(JavaEnumImpl::class.java, Files.newInputStream(Paths.get(file)))
                for (method in source.methods) {
                    val annotation = method.getAnnotation(
                        DbEnumValue::class.java
                    )
                    if (annotation != null) {
                        hasAnnotations.add(TypeInfoPojo(source.name, source.getPackage()))
                    }
                }
            } catch (ignored: ParserException) {
            }
        }
        val source: JavaClassSource
        val path = Paths.get(config.outFile + "/" + config.name + ".java")
        source = if (config.force || !path.toFile().exists()) {
            Roaster.create(JavaClassSource::class.java)
        } else {
            Roaster.parse(JavaClassSource::class.java, Files.newInputStream(path))
        }
        source.name = config.name
        if (config.packageName != null) {
            source.setPackage(config.packageName)
        }
        for (pojo in hasAnnotations) {
            var isImport = false
            if (!source.hasMethodSignature(pojo.typeName, String::class.java)) {
                source.addMethod().setPublic().setName(pojo.typeName).setReturnType(pojo.typeName)
                    .setBody("return str==null? null: " + pojo.typeName + ".find(str);")
                    .addParameter(String::class.java, "str")
                isImport = true
            }
            if (!source.hasMethodSignature(pojo.typeName, pojo.typeName)) {
                source.addMethod().setPublic().setName(pojo.typeName).setReturnType(String::class.java)
                    .setBody("return type==null? null: type.getValue();").addParameter(pojo.typeName, "type")
            }
            if (isImport) {
                source.addImport(java.lang.String.join(".", pojo.packagePath, pojo.typeName))
            }
        }
        val javaDoc: JavaDocSource<*> = source.javaDoc
        if (javaDoc.getTags("@author").size < 1) {
            javaDoc.addTagValue("@author", "start2do 自动生成")
        }
        val outputStream = Files.newOutputStream(path)
        outputStream.write(source.toString().toByteArray(StandardCharsets.UTF_8))
        outputStream.flush()
        outputStream.close()
    }


}