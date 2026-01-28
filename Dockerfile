# -------- Stage 1: Build --------
FROM amazoncorretto:21-alpine-jdk AS build
WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw

# Cache dependencies
RUN ./mvnw dependency:go-offline

# Copy sources and build
COPY src src
RUN ./mvnw clean package -DskipTests


# -------- Stage 2: Runtime --------
FROM amazoncorretto:21-alpine-jdk
WORKDIR /app

# Optional but recommended for Spring Boot
VOLUME /tmp

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=prod"]
