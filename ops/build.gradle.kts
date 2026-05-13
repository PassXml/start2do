plugins {
    id("java")
}

group = "org.start2do"
version = "2.0.9.2.14-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.apache.commons:commons-compress");
    testImplementation("org.aspectj:aspectjrt:1.9.7");
    testImplementation("org.aspectj:aspectjweaver:1.9.7");
    testImplementation("org.springframework.boot:spring-boot-starter-web");
    testImplementation("jakarta.validation:jakarta.validation-api");
    testImplementation(project(":spring-common"))
    testImplementation(project(":spring-util"))
    testImplementation(project(":common-util"))
    testImplementation(project(":bean-valiate"))
    testImplementation("org.slf4j:slf4j-api")
    testImplementation("org.slf4j:slf4j-simple")
//
    compileOnly("org.apache.commons:commons-compress");
    compileOnly("org.aspectj:aspectjrt:1.9.7")
    compileOnly("org.springframework.boot:spring-boot-starter-web");
    compileOnly("jakarta.validation:jakarta.validation-api");
    compileOnly(project(":spring-common"))
    compileOnly(project(":spring-util"))
    compileOnly(project(":common-util"))
}

tasks.test {
    useJUnitPlatform()
}
