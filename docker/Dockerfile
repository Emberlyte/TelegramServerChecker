FROM gradle:8.14.3-jdk21 AS builder

WORKDIR /app

COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

RUN chmod +x gradlew

RUN ./gradlew dependencies --no-daemon

COPY src ./src

RUN ./gradlew build -x test --no-daemon

FROM openjdk:21

WORKDIR /app

ARG SPRING_PROFILE=server

ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILE}

COPY --from=builder /app/build/libs/*.jar app.jar


ENTRYPOINT ["java", "-jar", "app.jar"]
