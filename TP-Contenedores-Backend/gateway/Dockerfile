FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /app_build

COPY pom.xml .
COPY common-data/pom.xml common-data/pom.xml
COPY gateway/pom.xml gateway/pom.xml
COPY ms-transporte/pom.xml ms-transporte/pom.xml
COPY ms-geolocalizacion/pom.xml ms-geolocalizacion/pom.xml
COPY ms-solicitudes/pom.xml ms-solicitudes/pom.xml

COPY gateway/src gateway/src

RUN mvn -pl gateway -am -DskipTests package

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=builder /app_build/gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]