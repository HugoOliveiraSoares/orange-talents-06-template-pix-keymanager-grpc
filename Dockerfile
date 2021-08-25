FROM openjdk:11
EXPOSE 50051
ARG JAR_FILE=build/libs/*-all.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]