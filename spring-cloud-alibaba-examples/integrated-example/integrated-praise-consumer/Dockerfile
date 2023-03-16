FROM openjdk:8-jdk-alpine as builder

LABEL author="yuluo" \
	email="yuluo829@aliyun.com"

ADD ./integrated-praise-consumer/target/integrated-praise-consumer-*.jar /app.jar

RUN sh -c 'touch /app.jar'
EXPOSE 8014

ENTRYPOINT ["java", "-jar","/app.jar"]