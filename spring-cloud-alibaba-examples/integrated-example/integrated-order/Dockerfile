FROM openjdk:8-jdk-alpine as builder

LABEL author="yuluo" \
	email="yuluo829@aliyun.com"

ADD ./integrated-order/target/integrated-order-*.jar /app.jar

RUN sh -c 'touch /app.jar'
EXPOSE 8013

ENTRYPOINT ["java", "-jar","/app.jar"]