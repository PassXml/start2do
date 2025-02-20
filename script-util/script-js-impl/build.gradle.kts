plugins {
    id("java")
    id("java-library")
}

group = "org.start2do"
version = "2.0.9.2.10-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    compileOnly("org.graalvm.polyglot:js")
    compileOnly(project(":common-util"))
    compileOnly("org.slf4j:slf4j-api:2.0.5")
    compileOnly(project(":script-util"))
    compileOnly("org.graalvm.polyglot:polyglot")
    testImplementation("org.graalvm.polyglot:polyglot")
    testImplementation("org.graalvm.polyglot:js")
}

tasks.test {
    useJUnitPlatform()
}
