# Build stage - JAVA 21
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy các file cần thiết
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download dependencies (có thể bỏ qua nếu mất network)
RUN chmod +x mvnw && \
    ./mvnw dependency:go-offline -B || true  # Continue even if fails

# Copy source code
COPY src src

# Build với skip tests
RUN ./mvnw clean package -DskipTests -Dmaven.test.skip=true

# Run stage - JAVA 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Tạo thư mục uploads
RUN mkdir -p /tmp/uploads

# Memory settings
ENV JAVA_OPTS="-Xmx256m -Xms128m"
ENV SPRING_PROFILES_ACTIVE="railway"
ENV TZ="UTC"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -jar app.jar"]