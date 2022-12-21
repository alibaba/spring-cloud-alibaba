FROM openjdk:17
ADD /target/integrated-praise-provider-*.jar /app.jar
RUN bash -c 'touch /app.jar'
EXPOSE 8015
ENTRYPOINT ["java", "-jar","/app.jar"]