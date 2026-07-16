# Microservicios Reactivos Spring Boot AWS Lambda

[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Gradle](https://img.shields.io/badge/Gradle-9.6.1-02303A.svg?logo=gradle)](https://gradle.org/)
[![AWS Lambda](https://img.shields.io/badge/AWS-Lambda-FF9900.svg?logo=amazon-aws)](https://aws.amazon.com/lambda/)
[![License](https://img.shields.io/badge/license-Copyright-blue.svg)](LICENSE)

Código fuente del ebook **"Microservicios Reactivos con Spring Boot y AWS Lambda"**. Proyecto completo con Spring Boot 4.1.0, Java 25, Project Reactor y AWS Lambda. Listo para producción con soporte para compilación nativa con GraalVM.

> 📖 **Obtén el ebook completo:**
> - 📚 [Amazon Kindle](https://www.amazon.com/dp/B0G1L1FFK6)
> - 🛒 [Hotmart](https://go.hotmart.com/O102857613J?dp=1)
> - 💳 [Gumroad](https://marcoslozina.gumroad.com/l/tporu)

## 🚀 Stack Tecnológico

- **Java 25** + **Spring Boot 4.1.0** + **Spring WebFlux** *(Ebook: Sección 0.13, 0.14)*
- **Spring Cloud Function 5.0.3** (Spring Cloud 2025.1.2) *(Ebook: Sección 3.2)*
- **Project Reactor** (Mono/Flux) *(Ebook: Sección 2.3)*
- **Spring Cloud Function** + **AWS Lambda** *(Ebook: Sección 3.3, 3.4)*
- **GraalVM Native Image** (compilación nativa) *(Ebook: Sección 4.2, 4.4)*
- **AWS SAM** + **LocalStack** (desarrollo local) *(Ebook: Sección 1.5, 3.6)*
- **GitHub Actions** (CI/CD) *(Ebook: Sección 6.6)*
- **Micrometer** + **Spring Actuator** (observabilidad) *(Ebook: Sección 6.4)*

## 🏗️ Arquitectura

```
API Gateway HTTP API → AWS Lambda → Spring Cloud Function → Project Reactor
                                    ↓
                            CloudWatch Logs / DynamoDB / SQS
```

## 🚀 Inicio Rápido

**Requisitos:** Java 25, Gradle 9.6.1+ (incluido), Docker, AWS SAM CLI *(Ebook: Sección 1.2)*

```bash
# Clonar y compilar
git clone <repository-url>
cd reactive-microservices-aws-lambda-java25

# Usar la versión con Java 25 (recomendado)
git checkout v1.0.0-java25

# O usar la versión anterior con Java 21 (solo para referencia)
# git checkout v1.0.0-java21

./gradlew clean build  # Ebook: Sección 1.4, 1.8

# Ejecutar tests
./gradlew test  # Ebook: Sección 2.8

# Levantar aplicación local
./gradlew :lambda-core:bootRun  # Ebook: Sección 1.6
```

## 💻 Desarrollo Local

```bash
# Iniciar LocalStack
docker-compose up -d localstack  # Ebook: Sección 1.5, 5.10

# Probar con SAM
cd lambda-infra
sam build --template template.yaml  # Ebook: Sección 3.6
sam local invoke "ReactiveFunction" --event events/hello.json  # Ebook: Sección 3.6
sam local start-api  # Ebook: Sección 3.6
```

## 🐳 Ejecución con Docker

### Imagen de la aplicación

```bash
# Construir imagen multi-stage (Java 25 + Spring Boot)
docker build -t reactive-lambda .

# Levantar la función como servicio WebFlux en localhost (usa 8081 si 8080 está ocupado)
docker run --rm -d -p 8081:8080 --name reactive-lambda reactive-lambda

# Smoke test sobre el endpoint y los actuators expuestos en el contenedor
curl -i "http://localhost:8081/hello?name=Marcos"
curl -i "http://localhost:8081/actuator/health"
```

Ejemplo de respuesta:

```
HTTP/1.1 200 OK
Content-Type: application/json
{"message":"ok","name":"Marcos","greeting":"Hello, Marcos!","timestamp":"2025-12-07T22:25:41.234Z"}
```

### Tooling containerizado (Gradle + SAM)

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

## ☁️ Despliegue a AWS

```bash
cd lambda-infra
sam deploy --guided  # Ebook: Sección 3.7, 3.11.1
```

> 📖 **Ver en el ebook:** 
> - Sección 3.7 (Despliegue en AWS)
> - Sección 3.11.1 (Despliegue con API Gateway)
> - Sección 4.8 (Despliegue del binario nativo en AWS Lambda)

## 🧪 Testing

```bash
./gradlew test                    # Todos los tests
./gradlew :lambda-core:test       # Solo lambda-core
./gradlew :lambda-tests:test      # Solo integración
```

## 🎯 Compilación Nativa

```bash
export JAVA_HOME=/path/to/graalvm-jdk-25  # Ebook: Sección 4.5.1
./gradlew :lambda-core:nativeCompile  # Ebook: Sección 4.4, 4.8
```

> 📖 **Ver en el ebook:** Sección 4 (Optimización de arranque y performance con GraalVM Native) para detalles completos sobre configuración, optimizaciones y despliegue del binario nativo.

## 📊 Observabilidad

```bash
# Health check
curl http://localhost:8080/actuator/health  # Ebook: Sección 6.4

# Métricas
curl http://localhost:8080/actuator/metrics  # Ebook: Sección 6.4
```

> 📖 **Ver en el ebook:** 
> - Sección 6.3 (Logging estructurado)
> - Sección 6.4 (Métricas personalizadas con Micrometer)
> - Sección 6.5 (Trazas distribuidas con AWS X-Ray)

## 🐛 Troubleshooting

**Error: "Cannot find handler"**
```bash
./gradlew clean build
```

**LocalStack no responde**
```bash
docker-compose restart localstack
curl http://localhost:4566/_localstack/health
```

**GraalVM Native Build falla**
```bash
export GRADLE_OPTS="-Xmx4g"
./gradlew clean :lambda-core:nativeCompile
```

## 📁 Estructura del Proyecto

```
├── lambda-core/          # Código principal Lambda (Ebook: Sección 1.7, 3.9)
│   ├── src/main/java/com/example/lambda/
│   │   ├── FunctionConfig.java      # Ebook: Sección 3.9.1
│   │   ├── HelloHandler.java        # Ebook: Sección 3.9.2
│   │   ├── HelloController.java     # Ebook: Sección 2.4, 2.5
│   │   ├── GlobalExceptionHandler.java  # Ebook: Sección 3.10, 2.7
│   │   └── RequestValidator.java   # Ebook: Sección 3.10.1
│   └── src/main/resources/
│       ├── application.yml          # Ebook: Sección 1.9.1
│       ├── application-dev.yml       # Ebook: Sección 1.9.2
│       └── application-prod.yml     # Ebook: Sección 1.9.3
├── lambda-infra/         # Template SAM (Ebook: Sección 3.11)
│   ├── template.yaml     # Ebook: Sección 3.11
│   └── events/           # Ebook: Sección 3.6
├── lambda-tests/         # Tests de integración (Ebook: Sección 2.8)
└── buildSrc/             # Convenciones Gradle (Ebook: Sección 1.4)
    └── src/main/kotlin/conventions.gradle.kts  # Ebook: Sección 0.14, 1.4
```

> 📖 **Documento de Referencias Cruzadas:** Ver [`REFERENCIAS_CRUZADAS_EBOOK_PROYECTO.md`](REFERENCIAS_CRUZADAS_EBOOK_PROYECTO.md) para mapeo completo entre el ebook y los archivos del proyecto.

## 🏷️ Versiones y Tags

El proyecto incluye tags de Git para facilitar el acceso a diferentes versiones:

- **`v1.0.0-java25`** (actual): Versión completa migrada a Java 25
  - Java 25 LTS, Spring Boot 3.4.13, Gradle 9.2.1
  - Runtime AWS Lambda: `java25`
  - Recomendado para seguir el ebook actualizado

- **`v1.0.0-java21`**: Última versión antes de la migración a Java 25
  - Java 21, Spring Boot 3.3.1
  - Útil para comparar cambios o ver el estado anterior

### Usar una versión específica

```bash
# Ver todas las versiones disponibles
git tag -l

# Cambiar a la versión con Java 25 (recomendado)
git checkout v1.0.0-java25

# Cambiar a la versión con Java 21 (solo referencia)
git checkout v1.0.0-java21

# Ver diferencias entre versiones
git diff v1.0.0-java21 v1.0.0-java25

# Volver a la última versión
git checkout main
```

## 📚 Recursos

### 📖 Ebook
- 📚 [Amazon Kindle](https://www.amazon.com/dp/B0G1L1FFK6)
- 🛒 [Hotmart](https://go.hotmart.com/O102857613J?dp=1)
- 💳 [Gumroad](https://marcoslozina.gumroad.com/l/tporu)

> 🔗 **Referencias Cruzadas:** Este proyecto está 100% alineado con el ebook. Consulta [`REFERENCIAS_CRUZADAS_EBOOK_PROYECTO.md`](REFERENCIAS_CRUZADAS_EBOOK_PROYECTO.md) para navegar entre el ebook y el código fuente.

### Documentación
- [Spring Boot](https://spring.io/projects/spring-boot) *(Ebook: Sección 0.13, 1.4)*
- [Spring Cloud Function](https://spring.io/projects/spring-cloud-function) *(Ebook: Sección 3.2)*
- [AWS SAM](https://docs.aws.amazon.com/serverless-application-model/) *(Ebook: Sección 3.6, 3.11)*
- [Project Reactor](https://projectreactor.io/docs/core/release/reference/) *(Ebook: Sección 2.3)*

## 💝 Apoyo al Proyecto

Si este proyecto te ha sido útil:

- 📖 **Obtén el ebook completo** - [Amazon](https://www.amazon.com/dp/B0G1L1FFK6) | [Hotmart](https://go.hotmart.com/O102857613J?dp=1) | [Gumroad](https://marcoslozina.gumroad.com/l/tporu)
- ☕ [Buy Me a Coffee](https://buymeacoffee.com/codefuel)
- 💳 [PayPal Donate](https://www.paypal.com/donate/?hosted_button_id=4TYGJ5S8CLX8J)
- ⭐ **Dale una estrella** al repositorio

¡Gracias por tu apoyo! 🙏

## 🤝 Contribuir

1. Fork el proyecto
2. Crea una rama (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📄 Licencia

Copyright (c) 2025 Marcos Raimundo Lozina. Todos los derechos reservados.

Este proyecto está protegido por derechos de autor. Ver el archivo [LICENSE](LICENSE) para detalles completos.

**Uso**: Solo para uso personal y educativo. Cualquier uso comercial requiere autorización previa.

## 👤 Autor

**Marcos Raimundo Lozina**

Creado como proyecto de referencia para microservicios reactivos serverless con Spring Boot y AWS Lambda.
