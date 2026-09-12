# Reactive Microservices Spring Boot AWS Lambda

[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Gradle](https://img.shields.io/badge/Gradle-9.6.1-02303A.svg?logo=gradle)](https://gradle.org/)
[![AWS Lambda](https://img.shields.io/badge/AWS-Lambda-FF9900.svg?logo=amazon-aws)](https://aws.amazon.com/lambda/)
[![License](https://img.shields.io/badge/license-Copyright-blue.svg)](LICENSE)

Source code for the ebook **"Reactive Microservices with Spring Boot and AWS Lambda"**. A complete project with Spring Boot 4.1.0, Java 25, Project Reactor, and AWS Lambda. Production-ready with support for native compilation via GraalVM.

> 📖 **Get the full ebook:**
> - 📚 [Amazon Kindle](https://www.amazon.com/dp/B0G1L1FFK6)
> - 🛒 [Hotmart](https://go.hotmart.com/O102857613J?dp=1)
> - 💳 [Gumroad](https://marcoslozina.gumroad.com/l/tporu)

## 🚀 Tech Stack

- **Java 25** + **Spring Boot 4.1.0** + **Spring WebFlux** *(Ebook: Section 0.13, 0.14)*
- **Spring Cloud Function 5.0.3** (Spring Cloud 2025.1.2) *(Ebook: Section 3.2)*
- **Project Reactor** (Mono/Flux) *(Ebook: Section 2.3)*
- **Spring Cloud Function** + **AWS Lambda** *(Ebook: Section 3.3, 3.4)*
- **GraalVM Native Image** (native compilation) *(Ebook: Section 4.2, 4.4)*
- **AWS SAM** + **LocalStack** (local development) *(Ebook: Section 1.5, 3.6)*
- **GitHub Actions** (CI/CD) *(Ebook: Section 6.6)*
- **Micrometer** + **Spring Actuator** (observability) *(Ebook: Section 6.4)*

## 🏗️ Architecture

```
API Gateway HTTP API → AWS Lambda → Spring Cloud Function → Project Reactor
                                    ↓
                            CloudWatch Logs / DynamoDB / SQS
```

## 🚀 Quick Start

**Requirements:** Java 25, Gradle 9.6.1+ (bundled), Docker, AWS SAM CLI *(Ebook: Section 1.2)*

```bash
# Clone and build
git clone <repository-url>
cd reactive-microservices-aws-lambda-java25

# Use the Java 25 version (recommended)
git checkout v1.0.0-java25

# Or use the previous Java 21 version (reference only)
# git checkout v1.0.0-java21

./gradlew clean build  # Ebook: Section 1.4, 1.8

# Run tests
./gradlew test  # Ebook: Section 2.8

# Start the local application
./gradlew :lambda-core:bootRun  # Ebook: Section 1.6
```

## 💻 Local Development

```bash
# Start LocalStack
docker-compose up -d localstack  # Ebook: Section 1.5, 5.10

# Test with SAM
cd lambda-infra
sam build --template template.yaml  # Ebook: Section 3.6
sam local invoke "ReactiveFunction" --event events/hello.json  # Ebook: Section 3.6
sam local start-api  # Ebook: Section 3.6
```

## 🐳 Running with Docker

### Application Image

```bash
# Build the multi-stage image (Java 25 + Spring Boot)
docker build -t reactive-lambda .

# Run the function as a WebFlux service on localhost (uses 8081 if 8080 is taken)
docker run --rm -d -p 8081:8080 --name reactive-lambda reactive-lambda

# Smoke test against the endpoint and actuators exposed on the container
curl -i "http://localhost:8081/hello?name=Marcos"
curl -i "http://localhost:8081/actuator/health"
```

Example response:

```
HTTP/1.1 200 OK
Content-Type: application/json
{"message":"ok","name":"Marcos","greeting":"Hello, Marcos!","timestamp":"2025-12-07T22:25:41.234Z"}
```

### Containerized Tooling (Gradle + SAM)

```bash
# Bash / zsh
docker run --rm -it \
  -v ${PWD}:/workspace \
  -w /workspace \
  public.ecr.aws/sam/build-java25:latest \
  bash -lc "./gradlew test"

docker run --rm -it \
  -v ${PWD}:/workspace \
  -w /workspace \
  public.ecr.aws/sam/build-java25:latest \
  bash -lc "sam build --template lambda-infra/template.yaml && sam validate"
```

```powershell
# PowerShell
docker run --rm -it `
  -v ${PWD}:/workspace `
  -w /workspace `
  public.ecr.aws/sam/build-java25:latest `
  bash -lc "./gradlew test"

docker run --rm -it `
  -v ${PWD}:/workspace `
  -w /workspace `
  public.ecr.aws/sam/build-java25:latest `
  bash -lc "sam build --template lambda-infra/template.yaml && sam validate"
```

## ☁️ Deploying to AWS

```bash
cd lambda-infra
sam deploy --guided  # Ebook: Section 3.7, 3.11.1
```

> 📖 **See in the ebook:**
> - Section 3.7 (Deploying to AWS)
> - Section 3.11.1 (Deploying with API Gateway)
> - Section 4.8 (Deploying the native binary to AWS Lambda)

## 🧪 Testing

```bash
./gradlew test                    # All tests
./gradlew :lambda-core:test       # lambda-core only
./gradlew :lambda-tests:test      # Integration only
```

## 🎯 Native Compilation

```bash
export JAVA_HOME=/path/to/graalvm-jdk-25  # Ebook: Section 4.5.1
./gradlew :lambda-core:nativeCompile  # Ebook: Section 4.4, 4.8
```

> 📖 **See in the ebook:** Section 4 (Startup and performance optimization with GraalVM Native) for full details on configuration, optimizations, and deploying the native binary.

## 📊 Observability

```bash
# Health check
curl http://localhost:8080/actuator/health  # Ebook: Section 6.4

# Metrics
curl http://localhost:8080/actuator/metrics  # Ebook: Section 6.4
```

> 📖 **See in the ebook:**
> - Section 6.3 (Structured logging)
> - Section 6.4 (Custom metrics with Micrometer)
> - Section 6.5 (Distributed tracing with AWS X-Ray)

## 🐛 Troubleshooting

**Error: "Cannot find handler"**
```bash
./gradlew clean build
```

**LocalStack not responding**
```bash
docker-compose restart localstack
curl http://localhost:4566/_localstack/health
```

**GraalVM Native Build fails**
```bash
export GRADLE_OPTS="-Xmx4g"
./gradlew clean :lambda-core:nativeCompile
```

## 📁 Project Structure

```
├── lambda-core/          # Main Lambda code (Ebook: Section 1.7, 3.9)
│   ├── src/main/java/com/example/lambda/
│   │   ├── FunctionConfig.java      # Ebook: Section 3.9.1
│   │   ├── HelloHandler.java        # Ebook: Section 3.9.2
│   │   ├── HelloController.java     # Ebook: Section 2.4, 2.5
│   │   ├── GlobalExceptionHandler.java  # Ebook: Section 3.10, 2.7
│   │   └── RequestValidator.java   # Ebook: Section 3.10.1
│   └── src/main/resources/
│       ├── application.yml          # Ebook: Section 1.9.1
│       ├── application-dev.yml       # Ebook: Section 1.9.2
│       └── application-prod.yml     # Ebook: Section 1.9.3
├── lambda-infra/         # SAM template (Ebook: Section 3.11)
│   ├── template.yaml     # Ebook: Section 3.11
│   └── events/           # Ebook: Section 3.6
├── lambda-tests/         # Integration tests (Ebook: Section 2.8)
└── buildSrc/             # Gradle conventions (Ebook: Section 1.4)
    └── src/main/kotlin/conventions.gradle.kts  # Ebook: Section 0.14, 1.4
```

> 📖 **Cross-Reference Document:** See [`REFERENCIAS_CRUZADAS_EBOOK_PROYECTO.md`](REFERENCIAS_CRUZADAS_EBOOK_PROYECTO.md) for a full mapping between the ebook and the project files.

## 🏷️ Versions and Tags

The project includes Git tags to make it easy to access different versions:

- **`v1.0.0-java25`** (current): Full version migrated to Java 25
  - Java 25 LTS, Spring Boot 3.4.13, Gradle 9.2.1
  - AWS Lambda runtime: `java25`
  - Recommended for following the updated ebook

- **`v1.0.0-java21`**: Last version before the Java 25 migration
  - Java 21, Spring Boot 3.3.1
  - Useful for comparing changes or seeing the prior state

### Using a Specific Version

```bash
# List all available versions
git tag -l

# Switch to the Java 25 version (recommended)
git checkout v1.0.0-java25

# Switch to the Java 21 version (reference only)
git checkout v1.0.0-java21

# See differences between versions
git diff v1.0.0-java21 v1.0.0-java25

# Go back to the latest version
git checkout main
```

## 📚 Resources

### 📖 Ebook
- 📚 [Amazon Kindle](https://www.amazon.com/dp/B0G1L1FFK6)
- 🛒 [Hotmart](https://go.hotmart.com/O102857613J?dp=1)
- 💳 [Gumroad](https://marcoslozina.gumroad.com/l/tporu)

> 🔗 **Cross-References:** This project is 100% aligned with the ebook. See [`REFERENCIAS_CRUZADAS_EBOOK_PROYECTO.md`](REFERENCIAS_CRUZADAS_EBOOK_PROYECTO.md) to navigate between the ebook and the source code.

### Documentation
- [Spring Boot](https://spring.io/projects/spring-boot) *(Ebook: Section 0.13, 1.4)*
- [Spring Cloud Function](https://spring.io/projects/spring-cloud-function) *(Ebook: Section 3.2)*
- [AWS SAM](https://docs.aws.amazon.com/serverless-application-model/) *(Ebook: Section 3.6, 3.11)*
- [Project Reactor](https://projectreactor.io/docs/core/release/reference/) *(Ebook: Section 2.3)*

## 💝 Support the Project

If this project has been useful to you:

- 📖 **Get the full ebook** - [Amazon](https://www.amazon.com/dp/B0G1L1FFK6) | [Hotmart](https://go.hotmart.com/O102857613J?dp=1) | [Gumroad](https://marcoslozina.gumroad.com/l/tporu)
- ☕ [Buy Me a Coffee](https://buymeacoffee.com/codefuel)
- 💳 [PayPal Donate](https://www.paypal.com/donate/?hosted_button_id=4TYGJ5S8CLX8J)
- ⭐ **Star** the repository

Thanks for your support! 🙏

## 🤝 Contributing

1. Fork the project
2. Create a branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

Copyright (c) 2025 Marcos Raimundo Lozina. All rights reserved.

This project is protected by copyright. See the [LICENSE](LICENSE) file for full details.

**Usage**: Personal and educational use only. Any commercial use requires prior authorization.

## 👤 Author

**Marcos Raimundo Lozina**

Created as a reference project for serverless reactive microservices with Spring Boot and AWS Lambda.
