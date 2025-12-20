# Plugin Ebean Fix

这个模块使用 ASM 字节码操作库，在**编译时**自动将 Ebean QueryBean 的属性访问转换为方法调用。

## 功能说明

在使用 Ebean QueryBean 时，原始代码可能会使用属性访问方式：

```java
// 原始写法（属性访问）
QUser qUser = QUser.alias();
query.where().eq(qUser.name, "John");
```

本模块会在编译时自动将其转换为方法调用方式：

```java
// 转换后（方法调用）
QUser qUser = QUser.alias();
query.where().eq(qUser._name(), "John");
```

## 使用方式

### 方式 1: Gradle 插件（推荐 - 编译时转换）

在项目的 `build.gradle` 中应用插件：

```gradle
plugins {
    id 'org.start2do.ebean-fix'
}

// 可选配置
ebeanFix {
    enabled = true           // 是否启用转换，默认 true
    failOnError = true       // 转换失败时是否中断构建，默认 true
    verbose = false          // 是否显示详细日志，默认 false
}
```

或者在多模块项目中：

```gradle
// 在需要使用的子模块中
apply plugin: 'org.start2do.ebean-fix'

dependencies {
    implementation project(':plugin-ebean-fix')
}
```

插件会自动在 `compileJava` 任务后执行转换。

### 方式 2: 手动配置（编译时转换）

如果不想使用插件，可以手动注册转换任务：

```gradle
import org.start2do.ebean.fix.gradle.EbeanFixTask

dependencies {
    implementation project(':plugin-ebean-fix')
}

// 为 compileJava 任务创建转换任务
tasks.register('ebeanFixMain', EbeanFixTask) {
    classesDir.set(tasks.compileJava.destinationDirectory)
    mustRunAfter tasks.compileJava
}

// 让编译任务完成后自动执行转换
tasks.compileJava.finalizedBy tasks.ebeanFixMain

// 如果有 compileTestJava，同样配置
tasks.register('ebeanFixTest', EbeanFixTask) {
    classesDir.set(tasks.compileTestJava.destinationDirectory)
    mustRunAfter tasks.compileTestJava
}
tasks.compileTestJava.finalizedBy tasks.ebeanFixTest
```

**注意**: 推荐使用方式 1（Gradle 插件），更简单且不易出错。

### 方式 3: 命令行工具（手动转换）

直接转换已编译的 class 文件：

```bash
java -cp plugin-ebean-fix.jar org.start2do.ebean.fix.QueryBeanTransformer <目录路径>
```

### 方式 4: Java Agent（运行时转换 - 不推荐）

在运行时动态转换类：

```bash
java -javaagent:plugin-ebean-fix.jar -jar your-application.jar
```

**注意**: 运行时转换会影响性能，建议使用编译时转换（方式 1 或 2）。

## 工作原理

1. Gradle 插件在 Java 编译任务（`compileJava`）完成后自动触发
2. 使用 ASM 库扫描编译输出目录中的所有 class 文件
3. 识别对 QueryBean 属性的 `GETFIELD` 字节码指令
4. 将其替换为 `INVOKEVIRTUAL` 指令，调用对应的 `_xxx()` 方法
5. 只转换 `io.ebean.typequery.*` 包下的 QueryBean 类型
6. 转换直接修改 class 文件，后续打包、运行都使用转换后的字节码

## 构建流程

```
源代码编写 (qUser.name)
  ↓
javac 编译
  ↓
生成 class 文件 (包含 GETFIELD 指令)
  ↓
plugin-ebean-fix 自动转换 ← 在这里执行转换
  ↓
修改 class 文件 (GETFIELD → INVOKEVIRTUAL)
  ↓
打包 jar
  ↓
运行程序 (使用转换后的字节码)
```

## 依赖

- ASM 9.7
- Ebean 14.8.1-javax (编译时依赖)

## 注意事项

1. **编译时机**: 插件会在每次 Java 编译后自动执行，无需额外配置
2. **增量编译**: 支持 Gradle 增量编译，只转换变更的类文件
3. **多模块支持**: 在需要的模块中单独应用插件即可
4. **性能影响**: 编译时转换对运行时性能无影响
5. **调试支持**: 转换后的字节码保持行号信息，支持正常调试
6. **IDEA 支持**: 转换发生在编译阶段，IDE 中的代码提示和检查不受影响

## 示例项目配置

假设你有一个使用 Ebean 的项目：

```gradle
// build.gradle
plugins {
    id 'java'
    id 'io.ebean' version '14.8.1'
    id 'org.start2do.ebean-fix'  // 添加此插件
}

dependencies {
    implementation 'io.ebean:ebean:14.8.1-javax'
    annotationProcessor 'io.ebean:querybean-generator:14.8.1-javax'
}

// 可选：配置转换行为
ebeanFix {
    enabled = true
    verbose = true  // 查看转换日志
}
```

编译输出示例：
```
> Task :compileJava
> Task :compileJava COMPLETED
[Ebean Fix Plugin] 开始转换 QueryBean 字段访问...
[Ebean Fix Plugin] 转换目录: /path/to/build/classes/java/main
  ✓ UserService.class (before: 2048 bytes, after: 2056 bytes)
  ✓ OrderService.class (before: 3072 bytes, after: 3080 bytes)
[Ebean Fix Plugin] ✓ QueryBean 转换完成
QueryBean transformation completed. Transformed 2 class files.
```

## 构建

```bash
./gradlew :plugin-ebean-fix:build
```

## 测试

模块包含了完整的单元测试，验证转换的正确性。
