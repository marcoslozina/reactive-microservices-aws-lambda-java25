import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot")
    id("org.graalvm.buildtools.native")
    jacoco
}

jacoco {
    toolVersion = "0.8.13"
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// Coverage budget: fails the build if instruction coverage on lambda-core drops
// below the threshold. Baseline measured on 2026-09-07 (HelloHandlerTest only,
// before this gate existed) was ~11.3% instruction coverage (72/639 instructions).
// Threshold set 5 points below that measured baseline so today's build passes
// with a small buffer, while still catching a real regression (e.g. tests
// deleted or a large amount of untested code added).
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.06".toBigDecimal()
            }
        }
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    finalizedBy(tasks.jacocoTestCoverageVerification)
}

configurations.all {
    resolutionStrategy {
        force("org.ow2.asm:asm:9.10.1")
        force("org.ow2.asm:asm-commons:9.10.1")
        force("org.ow2.asm:asm-tree:9.10.1")
        force("org.ow2.asm:asm-analysis:9.10.1")
    }
}

dependencies {
    // ASM 9.10.1 para soporte de Java 25
    implementation("org.ow2.asm:asm:9.10.1")
    implementation("org.ow2.asm:asm-commons:9.10.1")
    implementation("org.ow2.asm:asm-tree:9.10.1")
    implementation("org.ow2.asm:asm-analysis:9.10.1")

    // Spring Boot WebFlux
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Bean Validation - FALTANTE AGREGADO
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Spring Cloud Function
    implementation("org.springframework.cloud:spring-cloud-function-context")
    implementation("org.springframework.cloud:spring-cloud-starter-function-web")
    implementation("org.springframework.cloud:spring-cloud-function-adapter-aws")
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:2025.1.3"))

    // AWS Lambda Events
    implementation("com.amazonaws:aws-lambda-java-events:3.16.1")
    implementation("com.amazonaws:aws-lambda-java-serialization:1.1.6")
    // Declarada explicitamente: antes llegaba solo transitiva/opcional via
    // spring-cloud-function-adapter-aws y no terminaba en el classpath de native-image,
    // causando NoClassDefFoundError en RequestStreamHandler durante :lambda-core:nativeCompile.
    implementation("com.amazonaws:aws-lambda-java-core:1.4.0")

    // JSON Processing
    // NOTA: Spring Boot 4 usa Jackson 3 (tools.jackson) por defecto para su propio JSON
    // autoconfigurado, pero Spring Cloud Function (adapter AWS 5.0.x) internamente sigue
    // dependiendo de Jackson 2 (com.fasterxml.jackson.databind) para su conversion de
    // mensajes AWS Lambda (ver github.com/spring-cloud/spring-cloud-function issue #1337).
    // Mantenemos esta dependencia explicita para que ese bean siga disponible.
    implementation("com.fasterxml.jackson.core:jackson-databind")

    // Observability
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-core")

    // Logging
    implementation("ch.qos.logback:logback-classic")
    implementation("net.logstash.logback:logstash-logback-encoder:9.0")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.mockito:mockito-core")
}

graalvmNative {
    binaries {
        named("main") {
            imageName.set("lambda-core")
            // Entry point must be a class with a real `public static void main(String[])`.
            // org.springframework.cloud.function.adapter.aws.FunctionInvoker is a
            // RequestStreamHandler (used only as the AWS Lambda `Handler` for the JVM/Zip
            // deployment in lambda-infra/template.yaml) — it has no main() method. Passing it
            // as the native-image mainClass makes GraalVM's JDK 25 main-method resolution
            // (jdk.internal.misc.MethodFinder, JEP 512) return null instead of throwing, which
            // crashes native-image with a NullPointerException in
            // NativeImageGeneratorRunner.findDefaultJavaMainMethod instead of a clean error.
            // Point at the actual Spring Boot application class instead; Spring Cloud Function's
            // AWS adapter detects the Lambda custom runtime environment at startup and takes
            // over the AWS Lambda Runtime API event loop from there.
            mainClass.set("com.example.lambda.LambdaApplication")
            buildArgs.addAll(
                listOf(
                    "--no-fallback",
                    "--initialize-at-build-time",
                    "--initialize-at-run-time=org.springframework.cloud.function.adapter.aws.FunctionInvoker",
                    "--enable-url-protocols=http,https",
                    "-H:+IncludeAllLocales",
                    "-H:+ReportExceptionStackTraces",
                    "-H:IncludeResources=.*\\.properties",
                    "-H:IncludeResources=.*\\.yml",
                    "-H:IncludeResources=.*\\.yaml",
                    "-H:IncludeResources=.*\\.json",
                    "-H:IncludeResources=META-INF/native-image/.*"
                )
            )
        }
    }
}

tasks.named("nativeCompile") {
    dependsOn("bootJar")
}

tasks.withType<BootJar> {
    archiveFileName.set("lambda-core.jar")
}


