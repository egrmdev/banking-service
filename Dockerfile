FROM openjdk:17-slim

COPY build/libs/banking-service-*-SNAPSHOT.jar banking-service.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/banking-service.jar","--spring.config.location=classpath:application.yml"]