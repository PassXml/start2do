plugins {
    id("java")
}

group = "org.start2do"
version = "3.0.6.2-SNAPSHOT"


dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    compileOnly("org.springframework.data:spring-data-redis:${parent?.ext?.get("springBootVersion")}")
    api("org.apache.commons:commons-pool2")
    compileOnly(project(":common-redis"))
    compileOnly("com.fasterxml.jackson.core:jackson-databind:${parent?.ext?.get("jacksonVersion")}")

}

tasks.test {
    useJUnitPlatform()
}
