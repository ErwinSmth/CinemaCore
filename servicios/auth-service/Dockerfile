#Usa una imagen base de Java 17
FROM eclipse-temurin:17-jre-alpine

#Copia el jar generado por Maven
COPY target/*.jar app.jar

#Comando para ejecutar la aplicacion
ENTRYPOINT ["java","-jar","/app.jar"]