# Build stage - DÙNG JAVA 21 GIỐNG POM.XML
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copy các file cấu hình trước
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Set permissions và download dependencies
RUN chmod +x mvnw && \
    ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build với Java 21
RUN ./mvnw clean package -DskipTests

# Run stage - cũng dùng Java 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Thêm memory limit cho Railway
ENV JAVA_OPTS="-Xmx512m -Xms256m"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]