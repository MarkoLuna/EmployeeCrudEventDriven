FROM amazoncorretto:21.0.10-alpine3.21
EXPOSE 8080

COPY target/UsersService.jar /app.jar
CMD [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]

### commands
# mvn clean package -Dmaven.test.skip=true
# docker build -t users-service .
# docker run -p 8084:8080 --rm users-service
