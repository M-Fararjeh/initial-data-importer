FROM openjdk:8-jdk-alpine

# Install Maven
RUN apk add --no-cache maven

# Set working directory
WORKDIR /app

# Copy pom.xml first for better Docker layer caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Create logs directory
RUN mkdir -p /app/logs

# Expose port
EXPOSE 8080

# Set default JVM options (can be overridden by JAVA_OPTS environment variable)
ENV JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Run the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar target/data-import-service-1.0.0.jar"]