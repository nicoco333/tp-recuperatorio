FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY common-data/pom.xml common-data/pom.xml
COPY gateway/pom.xml gateway/pom.xml
COPY ms-flota/pom.xml ms-flota/pom.xml
COPY ms-rutas/pom.xml ms-rutas/pom.xml
COPY ms-solicitudes/pom.xml ms-solicitudes/pom.xml
COPY . .
RUN mvn -pl gateway -am -DskipTests package

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /workspace/gateway/target/gateway-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
