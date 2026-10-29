FROM eclipse-temurin:21-jdk AS builder
WORKDIR /build
COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY src ./src
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre
RUN useradd -r -u 1001 appuser
WORKDIR /app
COPY --from=builder /build/target/*.jar /app/app.jar
RUN mkdir -p /app/data/uploads && chown -R appuser:appuser /app
USER appuser
EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-jar","/app/app.jar"]
