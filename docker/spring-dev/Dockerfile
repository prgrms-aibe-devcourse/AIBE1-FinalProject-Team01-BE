# 빌드 전용 이미지
FROM gradle:8.5.0-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle build -x test

# 실행용 이미지
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=dev

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]