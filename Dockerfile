# ============================
# 1. Build stage (dùng Maven để build JAR)
# ============================
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# ============================
# 2. Run stage (chạy app bằng JDK 17)
# ============================
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Mặc định Spring Boot chạy port 8080
EXPOSE 8080

# Start app
ENTRYPOINT ["java","-jar","app.jar"]
