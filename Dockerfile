# Sử dụng OpenJDK image thay vì ubuntu trống
FROM openjdk:17-jdk-slim

# Đặt thư mục làm việc
WORKDIR /app

# Copy file jar đã build vào container
COPY target/*.jar app.jar

# Expose port (Render sẽ cần)
EXPOSE 8080

# Lệnh chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
