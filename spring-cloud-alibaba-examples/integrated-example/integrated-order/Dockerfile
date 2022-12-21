FROM openjdk:17
ADD /target/integrated-order-*.jar /app.jar
RUN bash -c 'touch /app.jar'
EXPOSE 8013
ENTRYPOINT ["java", "-jar","/app.jar"]