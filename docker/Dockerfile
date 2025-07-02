# 빌드 전용 이미지
FROM gradle:8.5.0-jdk17-alpine AS builder
WORKDIR /app
COPY . .
ENV DOCKER_BUILD=true
RUN gradle build -x test

# 실행용 이미지
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]