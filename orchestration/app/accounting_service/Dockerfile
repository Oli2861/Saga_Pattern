FROM gradle:7-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

FROM openjdk:17-oracle
COPY --from=build /home/gradle/src/build/libs/*fat.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
