FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache tzdata ffmpeg
ENV TZ=Asia/Seoul

WORKDIR /app

COPY build/libs/*.jar app.jar

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Duser.timezone=Asia/Seoul -Dspring.profiles.active=prod -jar app.jar"]
