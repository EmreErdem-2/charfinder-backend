# The latest OpenJDK Alpine image for a small, secure runtime
FROM amazoncorretto:25-jdk

# Set working directory
WORKDIR /app

# Copy the Spring Boot JAR into the container
COPY target/charfinder-app.jar app.jar

# Expose the port Spring Boot runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]