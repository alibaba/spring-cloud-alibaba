FROM openjdk:8
ADD /target/integrated-gateway-2.2.9-SNAPSHOT.jar /app.jar
RUN bash -c 'touch /app.jar'
EXPOSE 30010
ENTRYPOINT ["java", "-jar","/app.jar"]