# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy JAR từ stage build
COPY --from=build /app/target/*.jar app.jar

# Render sẽ truyền PORT qua env → dùng biến này
ENV PORT=8080
EXPOSE ${PORT}

CMD ["sh", "-c", "java -jar app.jar --server.port=${PORT}"]
