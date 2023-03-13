#!/bin/sh
echo "Nacos auto config started"
datasourceConfig=$(cat ../config/datasource-config.yaml)
storageConfig=$(cat ../config/integrated-storage.yaml)
accountConfig=$(cat ../config/integrated-account.yaml)
orderConfig=$(cat ../config/integrated-order.yaml)
gatewayConfig=$(cat ../config/integrated-gateway.yaml)
providerConfig=$(cat ../config/integrated-provider.yaml)
consumerConfig=$(cat ../config/integrated-consumer.yaml)
groupId="integrated-example"
curl -X POST "nacos-server:8848/nacos/v1/cs/configs" -d "dataId=datasource-config.yaml&group=${groupId}&content=${datasourceConfig}"
curl -X POST "nacos-server:8848/nacos/v1/cs/configs" -d "dataId=integrated-storage.yaml&group=${groupId}&content=${storageConfig}"
curl -X POST "nacos-server:8848/nacos/v1/cs/configs" -d "dataId=integrated-account.yaml&group=${groupId}&content=${accountConfig}"
curl -X POST "nacos-server:8848/nacos/v1/cs/configs" -d "dataId=integrated-order.yaml&group=${groupId}&content=${orderConfig}"
curl -X POST "nacos-server:8848/nacos/v1/cs/configs" -d "dataId=integrated-gateway.yaml&group=${groupId}&content=${gatewayConfig}"
curl -X POST "nacos-server:8848/nacos/v1/cs/configs" -d "dataId=integrated-provider.yaml&group=${groupId}&content=${providerConfig}"
curl -X POST "nacos-server:8848/nacos/v1/cs/configs" -d "dataId=integrated-consumer.yaml&group=${groupId}&content=${consumerConfig}"
echo "Nacos config pushed successfully finished"