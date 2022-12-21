FROM openjdk:17
ADD /target/integrated-account-*.jar /app.jar
RUN bash -c 'touch /app.jar'
EXPOSE 8012
ENTRYPOINT ["java", "-jar","/app.jar"]