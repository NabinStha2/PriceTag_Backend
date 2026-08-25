FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src src
RUN ./mvnw -B clean package -DskipTests


FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN groupadd --system spring \
  && useradd --system --gid spring spring

COPY --from=build --chown=spring:spring \
  /app/target/pricetag-*.jar app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]