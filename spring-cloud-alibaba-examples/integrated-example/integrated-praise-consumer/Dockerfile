FROM openjdk:17
ADD /target/integrated-praise-consumer-*.jar /app.jar
RUN bash -c 'touch /app.jar'
EXPOSE 8014
ENTRYPOINT ["java", "-jar","/app.jar"]