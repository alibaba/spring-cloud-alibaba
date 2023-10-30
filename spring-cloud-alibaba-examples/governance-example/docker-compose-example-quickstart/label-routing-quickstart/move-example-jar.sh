#!/bin/sh

gateway_example_project_path="label-routing-example/gateway-consumer-example/gateway-consumer-example/target"
gateway_example_docker_path="label-routing-gateway-consumer-example/label-routing-gateway-consumer-example"
zuul_example_project_path="label-routing-example/gateway-consumer-example/zuul-consumer-example/target"
zuul_example_docker_path="label-routing-gateway-consumer-example/label-routing-zuul-consumer-example"
feign_example_project_path="label-routing-example/web-client-consumer-example/openfeign-consumer-example/target"
feign_example_docker_path="label-routing-webClient-consumer-example/label-routing-feign-consumer-example"
rest_example_project_path="label-routing-example/web-client-consumer-example/restTemplate-consumer-example/target"
rest_example_docker_path="label-routing-webClient-consumer-example/label-routing-rest-consumer-example"
reactive_example_project_path="label-routing-example/web-client-consumer-example/webClient-consumer-example/target"
reactive_example_docker_path="label-routing-webClient-consumer-example/label-routing-reactive-consumer-example"
provider_example_project_path="label-routing-example/routing-service-provider-example/target"
provider_example_docker_path="label-routing-service-provider-example"


cp ../../${provider_example_project_path}/routing-service-provider-example-*.jar ./${provider_example_docker_path}/app.jar
cp ../../${gateway_example_project_path}/gateway-consumer-example-*.jar ./${gateway_example_docker_path}/app.jar
cp ../../${zuul_example_project_path}/zuul-consumer-example-*.jar ./${zuul_example_docker_path}/app.jar
cp ../../${feign_example_project_path}/openfeign-consumer-example-*.jar ./${feign_example_docker_path}/app.jar
cp ../../${rest_example_project_path}/restTemplate-consumer-example-*.jar ./${rest_example_docker_path}/app.jar
cp ../../${reactive_example_project_path}/webClient-consumer-example-*.jar ./${reactive_example_docker_path}/app.jar
