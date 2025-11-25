# Imagen base con Java 21 (JDK)
FROM eclipse-temurin:21-jdk

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo .jar compilado desde la carpeta target del host al contenedor
# El wildcard (*) hace que no tengamos que preocuparnos por el número de versión
COPY target/*.jar app.jar

# Expone el puerto en el que correrá la aplicación dentro del contenedor
EXPOSE 8080

# El comando que se ejecutará cuando el contenedor inicie
ENTRYPOINT ["java","-jar","app.jar"]