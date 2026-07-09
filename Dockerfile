FROM eclipse-temurin:25-alpine
COPY backend/target/backend-0.0.1.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]