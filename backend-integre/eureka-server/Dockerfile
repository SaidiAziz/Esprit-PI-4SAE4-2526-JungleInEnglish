FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
COPY src src

RUN mvn -B clean package -Dmaven.test.skip=true

FROM eclipse-temurin:17-jre
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=docker

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
