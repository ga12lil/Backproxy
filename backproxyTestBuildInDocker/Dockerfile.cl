FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY tests-1.0-SNAPSHOT.jar tests-1.0-SNAPSHOT.jar
EXPOSE 9080
ENTRYPOINT ["java", "-jar", "tests-1.0-SNAPSHOT.jar"]