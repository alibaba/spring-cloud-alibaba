# sh baseline.sh 2   or   sh baseline.sh 2 NACOS appactiveDemoNamespaceId
# sh baseline.sh 3

type=$1
channel=$2
tenant=$3
if  [ ! -n "$channel" ] ;then
    channel="FILE"
fi
echo "channel: ${channel}"


if [ `expr $type % 2` == 0 ]
then
  if [ $channel = "FILE" ]
  then
    for file in $(ls ../data/); do
      if [[ "$file" == *"path-address"* ]]; then
        echo "continue"
        continue
      fi
      echo "$(date "+%Y-%m-%d %H:%M:%S") 应用 ${file} 基线推送中";
      cp -f ./rule/idSource.json "../data/$file/"
      cp -f ./rule/transformerBetween.json "../data/$file/idTransformer.json"
      cp -f ./rule/idUnitMapping.json "../data/$file/"
      cp -f ./rule/dbProperty.json "../data/$file/mysql-product"
      arr=(${file//-/ })
      unitFlag=${arr[1]}
      echo "{\"unitFlag\":\"${unitFlag}\"}" > "../data/$file/machine.json"
      echo "$(date "+%Y-%m-%d %H:%M:%S") 应用 ${file} 基线推送完成"
    done
  elif [ $channel = "NACOS" ]
  then
    dataIdPrefix="appactive.dataId."
    groupId="appactive.groupId"

    idSourceRule=$(cat ./rule/idSource.json)
    echo "$(date "+%Y-%m-%d %H:%M:%S") idSourceRule 推送结果: " \
      && curl -X POST "127.0.0.1:8848/nacos/v1/cs/configs" \
      -d "tenant=${tenant}&dataId=${dataIdPrefix}idSourceRulePath&group=${groupId}&content=${idSourceRule}" \
      && echo ""

    idTransformerRule=$(cat ./rule/transformerBetween.json)
    echo "$(date "+%Y-%m-%d %H:%M:%S") idTransformerRule 推送结果: " \
      && curl -X POST "127.0.0.1:8848/nacos/v1/cs/configs" \
      -d "tenant=${tenant}&dataId=${dataIdPrefix}transformerRulePath&group=${groupId}&content=${idTransformerRule}" \
      && echo ""

    idUnitMappingRule=$(cat ./rule/idUnitMapping.json)
    echo "$(date "+%Y-%m-%d %H:%M:%S") idUnitMappingRule 推送结果: " \
      && curl -X POST "127.0.0.1:8848/nacos/v1/cs/configs" \
      -d "tenant=${tenant}&dataId=${dataIdPrefix}trafficRouteRulePath&group=${groupId}&content=${idUnitMappingRule}" \
      && echo ""

    forbiddenRule=$(cat ./rule/forbiddenRuleEmpty.json)
    echo "$(date "+%Y-%m-%d %H:%M:%S") forbiddenRule 推送结果: " \
      && curl -X POST "127.0.0.1:8848/nacos/v1/cs/configs" \
      -d "tenant=${tenant}&dataId=${dataIdPrefix}forbiddenRulePath&group=${groupId}&content=${forbiddenRule}" \
      && echo ""

    dataScopeRule=$(cat ./rule/dbProperty.json)
    echo "$(date "+%Y-%m-%d %H:%M:%S") dataScopeRule 推送结果: " \
      && curl -X POST "127.0.0.1:8848/nacos/v1/cs/configs" \
      -d "tenant=${tenant}&dataId=${dataIdPrefix}dataScopeRuleDirectoryPath_mysql-product&group=${groupId}&content=${dataScopeRule}" \
      && echo ""
  else
    echo "unsupported channel: ${channel}"
    exit 1
  fi
fi

if [ `expr $type % 3` == 0 ]
then
  idSource=$(cat ./rule/idSource.json)
  idTransformer=$(cat ./rule/idTransformer.json)
  idUnitMapping=$(cat ./rule/idUnitMapping.json)

  gatewayRule="{\"idSource\" : $idSource, \"idTransformer\" : $idTransformer, \"idUnitMapping\" : $idUnitMapping}"
  data="{\"key\" : \"459236fc-ed71-4bc4-b46c-69fc60d31f18_test1122\", \"value\" : $gatewayRule}"
  echo $data
  echo "$(date "+%Y-%m-%d %H:%M:%S") gateway 基线推送结果: " && curl --header "Content-Type: application/json" \
  --request POST \
  --data "$data" \
  127.0.0.1:8090/set
fi



