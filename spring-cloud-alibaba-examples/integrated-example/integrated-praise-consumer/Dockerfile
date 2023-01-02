FROM openjdk:8
ADD /target/integrated-praise-consumer-2.2.9-SNAPSHOT.jar /app.jar
RUN bash -c 'touch /app.jar'
EXPOSE 8014
ENTRYPOINT ["java", "-jar","/app.jar"]