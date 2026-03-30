FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache tzdata ffmpeg
ENV TZ=Asia/Seoul

WORKDIR /app

COPY build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Xmx384m", "-Xms128m", "-Duser.timezone=Asia/Seoul", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
