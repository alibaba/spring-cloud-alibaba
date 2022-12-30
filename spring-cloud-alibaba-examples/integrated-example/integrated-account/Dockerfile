FROM openjdk:8
ADD /target/integrated-account-2.2.9-SNAPSHOT.jar /app.jar
RUN bash -c 'touch /app.jar'
EXPOSE 8012
ENTRYPOINT ["java", "-jar","/app.jar"]