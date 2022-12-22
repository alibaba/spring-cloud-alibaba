FROM openjdk:17
ADD /target/integrated-gateway-*.jar /app.jar
RUN bash -c 'touch /app.jar'
EXPOSE 30010
ENTRYPOINT ["java", "-jar","/app.jar"]