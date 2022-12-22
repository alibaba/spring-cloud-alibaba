FROM openjdk:17
ADD /target/integrated-storage-*.jar /app.jar
RUN bash -c 'touch /app.jar'
EXPOSE 8011
ENTRYPOINT ["java", "-jar","/app.jar"]