# Build stage
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Copy Maven wrapper and project configuration
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Ensure the Maven wrapper script is executable
RUN chmod +x mvnw

# Download dependencies (this caches them in a separate Docker layer)
RUN ./mvnw dependency:go-offline

# Copy the actual application source code
COPY src/ src/

# Build the application
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the default port (Render will inject this at runtime)
ENV PORT=8080
EXPOSE ${PORT}

# Run the application
# We use shell form to ensure the ${PORT} environment variable expands correctly
CMD java -Dserver.port=${PORT} -jar app.jar
