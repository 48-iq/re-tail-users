FROM maven:3.9.9-eclipse-temurin-21-jammy AS build
WORKDIR /app
COPY ./pom.xml ./pom.xml
RUN mvn dependency:resolve
COPY ./src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre-jammy
COPY --from=build /app/target/*.jar ./app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]