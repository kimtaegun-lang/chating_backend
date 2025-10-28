# ===== 빌드 스테이지 =====
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Gradle wrapper와 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 소스 코드 복사
COPY src src

# 실행 권한 부여 및 빌드
RUN chmod +x gradlew
RUN ./gradlew clean build -x test

# ===== 실행 스테이지 =====
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 빌드된 JAR 파일만 복사
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]