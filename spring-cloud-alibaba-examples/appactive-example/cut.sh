channel=$1
tenant=$2
waitTime=$3
if  [ ! -n "$channel" ] ;then
    channel="FILE"
fi
if  [ ! -n "$waitTime" ] ;then
    waitTime=3
fi
echo "channel: ${channel}"
dataIdPrefix="appactive.dataId."
groupId="appactive.groupId"

forbiddenFile="forbiddenRule.json"
forbiddenFileEmpty="forbiddenFileEmpty.json"
idUnitMappingNextFile="idUnitMappingNext.json"

if [ $channel = "FILE" ]
then
  for file in $(ls ../data/); do
    if [[ "$file" == *"path-address"* ]]; then
      echo "continue"
      continue
    fi
    echo "$(date "+%Y-%m-%d %H:%M:%S") 应用 ${file} 禁写规则推送中)"
    cp -f ./rule/$forbiddenFile "../data/$file/forbiddenRule.json"
    echo "$(date "+%Y-%m-%d %H:%M:%S") 应用 ${file} 禁写规则推送完成"
  done
elif [ $channel = "NACOS" ]
then
    forbiddenRule=$(cat ./rule/forbiddenRule.json)
    echo "$(date "+%Y-%m-%d %H:%M:%S") forbiddenRule 推送结果: " \
      && curl -X POST "127.0.0.1:8848/nacos/v1/cs/configs" \
      -d "tenant=${tenant}&dataId=${dataIdPrefix}forbiddenRulePath&group=${groupId}&content=${forbiddenRule}" \
      && echo ""
else
  echo "unsupported channel: ${channel}"
  exit 1
fi

idSource=$(cat ./rule/idSource.json)
idTransformer=$(cat ./rule/idTransformer.json)
idUnitMapping=$(cat ./rule/$idUnitMappingNextFile)

gatewayRule="{\"idSource\" : $idSource, \"idTransformer\" : $idTransformer, \"idUnitMapping\" : $idUnitMapping}"
data="{\"key\" : \"459236fc-ed71-4bc4-b46c-69fc60d31f18_test1122\", \"value\" : $gatewayRule}"
echo $data
echo "$(date "+%Y-%m-%d %H:%M:%S") gateway 新规则推送结果: " && curl --header "Content-Type: application/json" \
--request POST \
--data "$data" \
127.0.0.1:8090/set

echo "等待数据追平......"
sleep "${waitTime}s"
echo "数据已经追平，下发新规则......"

if [ $channel = "FILE" ]
then
  for file in $(ls ../data/); do
    if [[ "$file" == *"path-address"* ]]; then
      echo "continue"
      continue
    fi
    echo "$(date "+%Y-%m-%d %H:%M:%S") 应用 ${file} 新规则推送中"
    cp -f ./rule/$idUnitMappingNextFile "../data/$file/idUnitMapping.json"
    echo "$(date "+%Y-%m-%d %H:%M:%S") 应用 ${file} 新规则推送完成"
    echo "$(date "+%Y-%m-%d %H:%M:%S") 应用 ${file} 清除禁写规则推送中)"
    cp -f ./rule/$forbiddenFileEmpty "../data/$file/forbiddenRule.json"
    echo "$(date "+%Y-%m-%d %H:%M:%S") 应用 ${file} 清除禁写规则推送完成"
  done
elif [ $channel = "NACOS" ]
then
  idUnitMappingRule=$(cat ./rule/idUnitMappingNext.json)
  echo "$(date "+%Y-%m-%d %H:%M:%S") idUnitMappingRule 推送结果: " \
    && curl -X POST "127.0.0.1:8848/nacos/v1/cs/configs" \
    -d "tenant=${tenant}&dataId=${dataIdPrefix}trafficRouteRulePath&group=${groupId}&content=${idUnitMappingRule}" \
    && echo ""
  forbiddenRule=$(cat ./rule/forbiddenRuleEmpty.json)
  echo "$(date "+%Y-%m-%d %H:%M:%S") forbiddenRule 推送结果: " \
    && curl -X POST "127.0.0.1:8848/nacos/v1/cs/configs" \
    -d "tenant=${tenant}&dataId=${dataIdPrefix}forbiddenRulePath&group=${groupId}&content=${forbiddenRule}" \
    && echo ""
else
  echo "unsupported channel: ${channel}"
  exit 1
fi
