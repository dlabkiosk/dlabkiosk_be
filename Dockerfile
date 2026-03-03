FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Xmx384m", "-Xms128m", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
