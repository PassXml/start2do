plugins {
    id("java")
}

group = "org.start2do"
version = "3.0.6.2-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    compileOnly("org.springframework.data:spring-data-redis:${parent?.ext?.get("springBootVersion")}")
    compileOnly("com.fasterxml.jackson.core:jackson-databind:${parent?.ext?.get("jacksonVersion")}")
    compileOnly("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${parent?.ext?.get("jacksonVersion")}")
    compileOnly("jakarta.persistence:jakarta.persistence-api")

    testImplementation("com.fasterxml.jackson.core:jackson-databind:${parent?.ext?.get("jacksonVersion")}")
    testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${parent?.ext?.get("jacksonVersion")}")
    testImplementation("com.fasterxml.jackson.core:jackson-annotations:${parent?.ext?.get("jacksonVersion")}")
    testImplementation("javax.persistence:javax.persistence-api:2.2")
    testImplementation("org.springframework.data:spring-data-redis:${parent?.ext?.get("springBootVersion")}")

}

tasks.test {
    useJUnitPlatform()
}
