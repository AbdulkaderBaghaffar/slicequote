FROM eclipse-temurin:21-jdk AS builder
WORKDIR /build
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -DskipTests dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre

RUN apt-get update && apt-get install -y --no-install-recommends \
      curl ca-certificates libgl1 libglu1-mesa libgtk-3-0 \
      libglib2.0-0 libfontconfig1 libx11-6 libxext6 libxrender1 libdbus-1-3 \
      libwebkit2gtk-4.1-0 \
    && rm -rf /var/lib/apt/lists/*

ARG PRUSA_VERSION=2.8.1
RUN curl -fsSL -o /tmp/p.AppImage \
      "https://github.com/prusa3d/PrusaSlicer/releases/download/version_2.8.1/PrusaSlicer-2.8.1%2Blinux-x64-newer-distros-GTK3-202409181416.AppImage" \
    && chmod +x /tmp/p.AppImage \
    && cd /opt && /tmp/p.AppImage --appimage-extract \
    && mv squashfs-root prusaslicer \
    && rm /tmp/p.AppImage
ENV SLICER_PATH=/opt/prusaslicer/AppRun

RUN useradd -r -u 1001 appuser

WORKDIR /app
COPY --from=builder /build/target/*.jar /app/app.jar
RUN mkdir -p /app/uploads /app/config && chown -R appuser:appuser /app

ENV HOME=/app/config
USER appuser
EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-jar","/app/app.jar"]
