#!/bin/sh

cp ../../routing-provider-example/target/routing-provider-example-*.jar ./routing-service-provider-example/app.jar
cp ../../routing-consumer-example/routing-rest-consumer-example/target/routing-rest-consumer-example-*.jar ./routing-webClient-consumer-example/routing-rest-consumer-example/app.jar
cp ../../routing-consumer-example/routing-reactive-consumer-example/target/routing-reactive-consumer-example-*.jar ./routing-webClient-consumer-example/routing-reactive-consumer-example/app.jar
cp ../../routing-consumer-example/routing-feign-consumer-example/target/routing-feign-consumer-example-*.jar ./routing-webClient-consumer-example/routing-feign-consumer-example/app.jar
cp ../../routing-gateway-consumer-example/routing-gateway-consumer-example/target/routing-gateway-consumer-example-*.jar ./routing-gateway-consumer-example/routing-gateway-consumer-example/app.jar
cp ../../routing-gateway-consumer-example/routing-zuul-consumer-example/target/routing-zuul-consumer-example-*.jar ./routing-gateway-consumer-example/routing-zuul-consumer-example/app.jar
