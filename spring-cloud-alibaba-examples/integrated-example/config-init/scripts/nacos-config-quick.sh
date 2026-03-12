#!/bin/sh
echo "Nacos auto config started"

config_dir="./config-init/config"
groupId="integrated-example"

# find all yaml config file then push to nacos
for config_file in $(find "${config_dir}" -type f -name "*.yaml"); do
    # use filename as dataId
    dataId=$(basename "$config_file")

    # read file content
    file_content=$(cat "$config_file")
    echo "\npublish config ${config_file}"

    # publish config to nacos
    curl -X POST "nacos-server:8848/nacos/v1/cs/configs" \
         -d "type=yaml" \
         --data-urlencode "dataId=${dataId}" \
         --data-urlencode "group=${groupId}" \
         --data-urlencode "content=${file_content}"
done

echo "\npublish Nacos config finished"
