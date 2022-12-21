FROM openjdk:17
ADD /target/integrated-frontend-*.jar /app.jar
RUN bash -c 'touch /app.jar'
EXPOSE 8080
ENTRYPOINT ["java", "-jar","/app.jar"]