FROM openjdk:11-jdk-alpine
VOLUME /main-app
ADD build/libs/springboot-api-transaction-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar","/app.jar"]