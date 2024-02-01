plugins {
    id("org.springframework.boot") version "2.7.4"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("java")
}

group = "org.ecocompass"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.jayway.jsonpath:json-path:2.6.0")
    implementation("org.jgrapht:jgrapht-core:1.5.2")
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:3.12.4") // Add Mockito for mocking
}

tasks.test {
    useJUnitPlatform()
}