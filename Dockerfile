FROM eclipse-temurin:21-jdk AS builder
WORKDIR /build
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN ./mvnw -B dependency:go-offline || true
COPY src ./src
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre

RUN apt-get update \
 && apt-get install -y --no-install-recommends prusa-slicer=2.9.4+dfsg-4 \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=builder /build/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
