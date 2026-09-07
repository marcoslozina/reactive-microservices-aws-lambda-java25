plugins {
    java
    id("io.spring.dependency-management") version "1.1.7"
    id("org.springframework.boot") version "4.1.1" apply false
    id("org.graalvm.buildtools.native") version "1.1.12" apply false
}

subprojects {
    group = "com.example"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }

    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "conventions")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:4.1.1")
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.1.3")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}


