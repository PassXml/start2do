plugins {
    id("java")
}

group = "org.start2do"
version = "2.0.9.2.10-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.apache.commons:commons-compress");
//
    compileOnly("org.apache.commons:commons-compress");
    compileOnly("org.springframework.boot:spring-boot-starter-web");
    compileOnly("jakarta.validation:jakarta.validation-api");
    compileOnly(project(":spring-common"))
    compileOnly(project(":spring-util"))
    compileOnly(project(":common-util"))
}

tasks.test {
    useJUnitPlatform()
}
