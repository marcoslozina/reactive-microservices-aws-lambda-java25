dependencies {
    implementation(project(":lambda-core"))
    implementation("com.amazonaws:aws-lambda-java-events:3.16.1")
    // Antes fijaba spring-boot-dependencies:3.3.1, desalineado del resto del proyecto (3.4.13/4.1.0).
    // Alineado a la misma version de Spring Boot que usa lambda-core para evitar choques de classpath
    // en los tests de integracion (mismo tipo de problema que jackson-databind/jackson-core desalineados).
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:4.1.1"))
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("io.projectreactor:reactor-test")
    // Gradle's JUnit Platform test runner needs the launcher explicitly on the runtime
    // classpath; spring-boot-starter-test doesn't always pull it in transitively here.
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


