package org.start2do.plugin.api.spring.annotation;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

/**
 * 根据插件中的注解，自动生成 PF4J 扩展实现类：
 * <ul>
 *   <li>{@link org.start2do.plugin.api.spring.SpringMvcControllerExtension}</li>
 *   <li>{@link org.start2do.plugin.api.spring.SpringPluginBeansExtension}</li>
 *   <li>{@link org.start2do.plugin.api.spring.MybatisMapperExtension}</li>
 *   <li>{@link org.start2do.plugin.api.spring.SoapWebServiceExtension}</li>
 * </ul>
 * <p>
 * 生成的类默认命名为 {@code PluginGeneratedExtensions}，位于首个被标记类型所在包。
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({
    "org.start2do.plugin.api.spring.annotation.PluginController",
    "org.start2do.plugin.api.spring.annotation.PluginBean",
    "org.start2do.plugin.api.spring.annotation.PluginMapper",
    "org.start2do.plugin.api.spring.annotation.PluginSoapService",
    "org.start2do.plugin.api.spring.annotation.PluginDatabase",
    // 支持可重复注解的容器注解 PluginDatabases
    "org.start2do.plugin.api.spring.annotation.PluginDatabases",
    "org.start2do.plugin.api.spring.annotation.PluginDescriptor"
})
public class PluginExtensionProcessor extends AbstractProcessor {

    private static final String GENERATED_SIMPLE_NAME = "PluginGeneratedExtensions";

    private final Set<TypeElement> controllerTypes = new LinkedHashSet<>();
    private final Set<TypeElement> beanTypes = new LinkedHashSet<>();
    private final Set<TypeElement> mapperTypes = new LinkedHashSet<>();
    private final Set<TypeElement> soapTypes = new LinkedHashSet<>();
    private final Set<TypeElement> databaseTypes = new LinkedHashSet<>();

    /**
     * 标记插件描述类（应当唯一）
     */
    private TypeElement pluginDescriptorElement;

    private boolean extensionsGenerated = false;
    private boolean descriptorGenerated = false;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 累积各轮中的注解元素（javac 可能会多轮触发处理器）
        collect(roundEnv, PluginController.class, controllerTypes, ElementKind.CLASS);
        collect(roundEnv, PluginBean.class, beanTypes, ElementKind.CLASS);
        collect(roundEnv, PluginMapper.class, mapperTypes, ElementKind.INTERFACE);
        collect(roundEnv, PluginSoapService.class, soapTypes, ElementKind.CLASS);
        // SOAP 实现类在运行时需要作为 Spring Bean 存在（Pf4jSoapBridge 通过 BeanName 获取实例），
        // 因此将 @PluginSoapService 也视为一种特殊的插件 Bean，无需插件端额外添加 @PluginBean。
        collect(roundEnv, PluginSoapService.class, beanTypes, ElementKind.CLASS);
        // 数据源类型需要同时处理单个 @PluginDatabase 和容器 @PluginDatabases 两种写法
        collect(roundEnv, PluginDatabase.class, databaseTypes, ElementKind.CLASS);
        collect(roundEnv, PluginDatabases.class, databaseTypes, ElementKind.CLASS);
        collectPluginDescriptor(roundEnv);

        // 1) 在非最后一轮尽早生成 PF4J 扩展实现类
        //    这样 PF4J 自带的 ExtensionAnnotationProcessor 能在后续轮次中
        //    看到 @Extension 注解，从而把生成类写入 META-INF/extensions.idx
        if (!roundEnv.processingOver()
            && !extensionsGenerated
            && !(controllerTypes.isEmpty()
            && beanTypes.isEmpty()
            && mapperTypes.isEmpty()
            && soapTypes.isEmpty()
            && databaseTypes.isEmpty())) {
            extensionsGenerated = true;
            try {
                generateExtensionClass();
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "生成 PluginGeneratedExtensions 失败: " + e.getMessage());
                throw new UncheckedIOException(e);
            }
        }

        // 2) 在最后一轮再生成 plugin.properties（只依赖 @PluginDescriptor）
        if (roundEnv.processingOver() && !descriptorGenerated && pluginDescriptorElement != null) {
            descriptorGenerated = true;
            try {
                generatePluginProperties();
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "生成 plugin.properties 失败: " + e.getMessage());
                throw new UncheckedIOException(e);
            }
        }

        // 返回 false 允许其他处理器继续处理这些注解
        return false;
    }

    private void collect(RoundEnvironment roundEnv,
        Class<? extends java.lang.annotation.Annotation> annotationType,
        Set<TypeElement> target,
        ElementKind expectedKind) {
        for (Element e : roundEnv.getElementsAnnotatedWith(annotationType)) {
            if (e.getKind() != expectedKind) {
                continue;
            }
            if (e instanceof TypeElement) {
                target.add((TypeElement) e);
            }
        }
    }

    /**
     * 收集插件描述信息（应当只有一个类带有 @PluginDescriptor）
     */
    private void collectPluginDescriptor(RoundEnvironment roundEnv) {
        for (Element e : roundEnv.getElementsAnnotatedWith(PluginDescriptor.class)) {
            if (e.getKind() != ElementKind.CLASS || !(e instanceof TypeElement)) {
                continue;
            }
            TypeElement type = (TypeElement) e;
            if (pluginDescriptorElement == null) {
                pluginDescriptorElement = type;
            } else if (!pluginDescriptorElement.equals(type)) {
                // 多个描述类时给出告警，仍然使用第一个
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                    "检测到多个 @PluginDescriptor，已忽略: " + type.getQualifiedName());
            }
        }
    }

    private void generateExtensionClass() throws IOException {
        TypeElement sample = pickSampleElement();
        String pkgName = resolvePackageName(sample);

        String fqn = pkgName.isEmpty()
            ? GENERATED_SIMPLE_NAME
            : pkgName + "." + GENERATED_SIMPLE_NAME;

        JavaFileObject fileObject = processingEnv.getFiler().createSourceFile(fqn);

        try (Writer writer = fileObject.openWriter()) {
            writer.write(renderSource(pkgName));
        }

        // 同步生成 PF4J 兼容的扩展索引文件，避免依赖 PF4J 自带注解处理器顺序问题
        generateExtensionsIndex(fqn);
    }

    /**
     * 生成 plugin.properties 文件，供 PF4J 使用 PropertiesPluginDescriptorFinder 读取。
     */
    private void generatePluginProperties() throws IOException {
        if (pluginDescriptorElement == null) {
            return;
        }

        PluginDescriptor desc = pluginDescriptorElement.getAnnotation(PluginDescriptor.class);
        if (desc == null) {
            return;
        }

        String pluginClass = pluginDescriptorElement.getQualifiedName().toString();
        String version = resolvePluginVersion(desc.version());

        FileObject fileObject = processingEnv.getFiler()
            .createResource(StandardLocation.CLASS_OUTPUT, "", "plugin.properties", pluginDescriptorElement);

        try (Writer writer = fileObject.openWriter()) {
            writer.write("plugin.id=" + escape(desc.id()) + "\n");
            writer.write("plugin.class=" + pluginClass + "\n");
            writer.write("plugin.version=" + escape(version) + "\n");
            if (!desc.provider().isEmpty()) {
                writer.write("plugin.provider=" + escape(desc.provider()) + "\n");
            }
            if (!desc.description().isEmpty()) {
            writer.write("plugin.description=" + escape(desc.description()) + "\n");
            }
        }
    }

    /**
     * 解析插件版本号。
     * <p>
     * 优先级：
     * 1) 注解处理器参数（-Aplugin.version=...）
     * 2) @PluginDescriptor.version
     * 3) 为空时使用构建时刻时间戳（毫秒）
     */
    private String resolvePluginVersion(String declared) {
        String option = processingEnv.getOptions().get("plugin.version");
        if (option != null && !option.trim().isEmpty()) {
            return option.trim();
        }
        if (declared != null && !declared.trim().isEmpty()) {
            return declared.trim();
        }
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * 直接生成 META-INF/extensions.idx，兼容 PF4J 的默认扩展发现机制。
     * <p>
     * 这样即使 PF4J 的 ExtensionAnnotationProcessor 没有参与当前编译过程，
     * 运行时的 DefaultExtensionFinder 仍然可以通过索引文件找到本插件的扩展实现类。
     */
    private void generateExtensionsIndex(String extensionFqn) throws IOException {
        try {
            FileObject fileObject = processingEnv.getFiler()
                .createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/extensions.idx");
            try (Writer writer = fileObject.openWriter()) {
                // 保持与 PF4J 默认格式一致
                writer.write("# Generated by PF4J\n");
                writer.write(extensionFqn);
                writer.write("\n");
            }
        } catch (FilerException ignored) {
            // 同一轮或增量编译可能已经创建过该文件，这种情况下直接跳过即可
        }
    }

    private TypeElement pickSampleElement() {
        if (!controllerTypes.isEmpty()) {
            return controllerTypes.iterator().next();
        }
        if (!beanTypes.isEmpty()) {
            return beanTypes.iterator().next();
        }
        if (!mapperTypes.isEmpty()) {
            return mapperTypes.iterator().next();
        }
        if (!soapTypes.isEmpty()) {
            return soapTypes.iterator().next();
        }
        if (!databaseTypes.isEmpty()) {
            return databaseTypes.iterator().next();
        }
        return null;
    }

    private String resolvePackageName(TypeElement element) {
        if (element == null) {
            return "org.start2do.plugin.generated";
        }
        PackageElement pkg = processingEnv.getElementUtils().getPackageOf(element);
        if (pkg == null || pkg.isUnnamed()) {
            return "org.start2do.plugin.generated";
        }
        return pkg.getQualifiedName().toString();
    }

    private String renderSource(String pkgName) {
        StringBuilder sb = new StringBuilder();

        if (pkgName != null && !pkgName.isEmpty()) {
            sb.append("package ").append(pkgName).append(";\n\n");
        }

        // 使用完全限定名，避免与插件内部类型发生命名冲突
        sb.append("@org.pf4j.Extension\n");
        sb.append("public final class ").append(GENERATED_SIMPLE_NAME).append(" implements ");

        List<String> interfaces = new ArrayList<>();
        if (!controllerTypes.isEmpty()) {
            interfaces.add("org.start2do.plugin.api.spring.SpringMvcControllerExtension");
        }
        if (!beanTypes.isEmpty()) {
            interfaces.add("org.start2do.plugin.api.spring.SpringPluginBeansExtension");
        }
        if (!mapperTypes.isEmpty()) {
            interfaces.add("org.start2do.plugin.api.spring.MybatisMapperExtension");
        }
        if (!soapTypes.isEmpty()) {
            interfaces.add("org.start2do.plugin.api.spring.SoapWebServiceExtension");
        }
        if (!databaseTypes.isEmpty()) {
            interfaces.add("org.start2do.plugin.api.spring.MybatisDataSourceExtension");
        }

        for (int i = 0; i < interfaces.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(interfaces.get(i));
        }
        sb.append(" {\n\n");

        if (!controllerTypes.isEmpty()) {
            renderControllerMethod(sb);
        }
        if (!beanTypes.isEmpty()) {
            renderBeanMethod(sb);
        }
        if (!mapperTypes.isEmpty()) {
            renderMapperMethod(sb);
        }
        if (!soapTypes.isEmpty()) {
            renderSoapMethod(sb);
        }
        if (!databaseTypes.isEmpty()) {
            renderDatabaseMethod(sb);
        }

        sb.append("}\n");
        return sb.toString();
    }

    private void renderControllerMethod(StringBuilder sb) {
        sb.append("    @Override\n");
        sb.append("    public java.util.Collection<Class<?>> getControllerClasses() {\n");
        sb.append("        java.util.List<Class<?>> list = new java.util.ArrayList<>();\n");
        for (TypeElement type : controllerTypes) {
            sb.append("        list.add(")
                .append(type.getQualifiedName().toString())
                .append(".class);\n");
        }
        sb.append("        return list;\n");
        sb.append("    }\n\n");
    }

    private void renderBeanMethod(StringBuilder sb) {
        sb.append("    @Override\n");
        sb.append("    public java.util.Collection<Class<?>> getBeanClasses() {\n");
        sb.append("        java.util.List<Class<?>> list = new java.util.ArrayList<>();\n");
        for (TypeElement type : beanTypes) {
            sb.append("        list.add(")
                .append(type.getQualifiedName().toString())
                .append(".class);\n");
        }
        sb.append("        return list;\n");
        sb.append("    }\n\n");
    }

    private void renderMapperMethod(StringBuilder sb) {
        sb.append("    @Override\n");
        sb.append("    public java.util.Collection<org.start2do.plugin.api.spring.MapperMeta> getMappers() {\n");
        sb.append(
            "        java.util.List<org.start2do.plugin.api.spring.MapperMeta> list = new java.util.ArrayList<>();\n");
        for (TypeElement type : mapperTypes) {
            PluginMapper ann = type.getAnnotation(PluginMapper.class);
            String dsId = ann != null ? ann.dataSourceId() : "";
            boolean useDefault = dsId == null || dsId.trim().isEmpty()
                || "primary".equals(dsId.trim());

            sb.append("        list.add(new org.start2do.plugin.api.spring.MapperMeta(");
            if (useDefault) {
                sb.append("org.start2do.plugin.api.spring.MapperMeta.DEFAULT_DATASOURCE_ID");
            } else {
                sb.append("\"").append(escape(dsId.trim())).append("\"");
            }
            sb.append(", ").append(type.getQualifiedName().toString()).append(".class));\n");
        }
        sb.append("        return list;\n");
        sb.append("    }\n\n");
    }

    private void renderSoapMethod(StringBuilder sb) {
        sb.append("    @Override\n");
        sb.append(
            "    public java.util.Collection<org.start2do.plugin.api.spring.SoapWebServiceExtension.WebServiceMeta> getWebServices() {\n");
        sb.append(
            "        java.util.List<org.start2do.plugin.api.spring.SoapWebServiceExtension.WebServiceMeta> list = new java.util.ArrayList<>();\n");
        for (TypeElement type : soapTypes) {
            PluginSoapService ann = type.getAnnotation(PluginSoapService.class);
            String address = ann != null ? ann.address() : "";
            String serviceName = ann != null ? ann.serviceName() : "";
            String portName = ann != null ? ann.portName() : "";
            String targetNs = ann != null ? ann.targetNamespace() : "";

            sb.append("        list.add(new org.start2do.plugin.api.spring.SoapWebServiceExtension.WebServiceMeta(")
                .append(type.getQualifiedName().toString())
                .append(".class, ");

            // address
            appendNullableStringLiteral(sb, address);
            sb.append(", ");
            // serviceName
            appendNullableStringLiteral(sb, serviceName);
            sb.append(", ");
            // portName
            appendNullableStringLiteral(sb, portName);
            sb.append(", ");
            // targetNamespace
            appendNullableStringLiteral(sb, targetNs);

            sb.append("));\n");
        }
        sb.append("        return list;\n");
        sb.append("    }\n\n");
    }

    private void renderDatabaseMethod(StringBuilder sb) {
        sb.append("    @Override\n");
        sb.append(
            "    public java.util.Collection<org.start2do.plugin.api.spring.PluginDatabaseMeta> getDatabases() {\n");
        sb.append(
            "        java.util.List<org.start2do.plugin.api.spring.PluginDatabaseMeta> list = new java.util.ArrayList<>();\n");
        for (TypeElement type : databaseTypes) {
            PluginDatabase[] anns = type.getAnnotationsByType(PluginDatabase.class);
            if (anns == null || anns.length == 0) {
                continue;
            }
            for (PluginDatabase ann : anns) {
                String id = ann.id();
                String url = ann.url();
                String username = ann.username();
                String password = ann.password();
                String driverClassName = ann.driverClassName();
                String mapperPattern = ann.mapperLocationPattern();
                String dataType = ann.dataType();
                // 注解处理阶段不要直接访问 Class 类型的注解属性，否则会触发 MirroredTypeException
                String dsClassName;
                try {
                    Class<?> dsClass = ann.dataSourceType();
                    dsClassName = (dsClass != null ? dsClass.getName() : "javax.sql.DataSource");
                } catch (MirroredTypeException ex) {
                    // 捕获 MirroredTypeException，从 TypeMirror 中安全获取类的完全限定名
                    TypeMirror mirror = ex.getTypeMirror();
                    dsClassName = (mirror != null ? mirror.toString() : "javax.sql.DataSource");
                }
                if (dsClassName == null || dsClassName.isEmpty()) {
                    dsClassName = "javax.sql.DataSource";
                }

                sb.append("        list.add(new org.start2do.plugin.api.spring.PluginDatabaseMeta(");
                sb.append("\"").append(escape(id.trim())).append("\"").append(", ");
                sb.append("\"").append(escape(url)).append("\"").append(", ");
                sb.append("\"").append(escape(username)).append("\"").append(", ");
                sb.append("\"").append(escape(password)).append("\"").append(", ");
                sb.append("\"").append(escape(driverClassName)).append("\"").append(", ");
                sb.append(dsClassName).append(".class").append(", ");

                if (mapperPattern == null || mapperPattern.isEmpty()) {
                    sb.append("null");
                } else {
                    sb.append("\"").append(escape(mapperPattern)).append("\"");
                }
                sb.append(", ");

                if (dataType == null || dataType.isEmpty()) {
                    sb.append("null");
                } else {
                    sb.append("\"").append(escape(dataType)).append("\"");
                }
                sb.append("));\n");
            }
        }
        sb.append("        return list;\n");
        sb.append("    }\n\n");
    }

    private void appendNullableStringLiteral(StringBuilder sb, String value) {
        if (value == null || value.isEmpty()) {
            sb.append("null");
        } else {
            sb.append("\"").append(escape(value)).append("\"");
        }
    }

    /**
     * 將原始字符串轉義為適合同時用於 Java 字面量與 properties 文件的格式。
     * <p>
     * 規則：
     * <ul>
     *   <li>反斜杠與雙引號前置反斜杠</li>
     *   <li>換行、回車、製表符轉為 \n / \r / \t</li>
     *   <li>所有非可打印 ASCII 字符（含中文）轉為 \\uXXXX 形式，保證 properties 按 ISO-8859-1 讀取時不會亂碼</li>
     * </ul>
     * <p>
     * - 對於 plugin.properties：java.util.Properties 讀取時會自動將 \\uXXXX 解析回原始字符<br>
     * - 對於生成的 Java 源碼：編譯器也會按 \\uXXXX 轉義得到正確的運行時字符串
     */
    private String escape(String raw) {
        if (raw == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(raw.length() + 16);
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            switch (c) {
                case '\\':
                case '\"':
                    sb.append('\\').append(c);
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    // 非可打印 ASCII（包含中文）統一輸出為 \\uXXXX，避免 properties 編碼問題
                    if (c < 0x20 || c > 0x7e) {
                        sb.append(String.format("\\u%04X", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
