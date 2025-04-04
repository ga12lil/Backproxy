FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY tests-2.0-SNAPSHOT.jar tests-2.0-SNAPSHOT.jar
EXPOSE 9081
ENTRYPOINT ["java", "-jar", "tests-2.0-SNAPSHOT.jar"]