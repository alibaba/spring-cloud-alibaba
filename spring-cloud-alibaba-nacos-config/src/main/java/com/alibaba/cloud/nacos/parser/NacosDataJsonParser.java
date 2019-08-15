package com.alibaba.cloud.nacos.parser;

import com.alibaba.nacos.client.utils.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * @Author: zkz
 */
public class NacosDataJsonParser extends AbstractNacosDataParser {
    protected NacosDataJsonParser() {
        super("json");
    }

    @Override
    protected Properties doParse(String data) throws IOException {
        if(StringUtils.isEmpty(data)){
            return null;
        }
        Map<String, String> map = parseJSON2Map(data);
        return this.generateProperties(this.reloadMap(map));
    }


    /**
     * JSON 类型的字符串转换成 Map
     */
    public static Map<String, String> parseJSON2Map(String json) throws IOException {
        Map<String, String> map = new HashMap<>(32);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(json);
        if(null == jsonNode){
            return map;
        }
        parseJsonNode(map,jsonNode,"");
        return map;
    }


    private static void parseJsonNode(Map<String, String> jsonMap, JsonNode jsonNode, String parentKey){
        Iterator<String> fieldNames = jsonNode.fieldNames();
        while (fieldNames.hasNext()) {
            String name = fieldNames.next();
            String fullKey = StringUtils.isEmpty(parentKey) ? name : parentKey + DOT + name;
            JsonNode resultValue = jsonNode.findValue(name);
            if(null == resultValue){
                continue;
            }
            if (resultValue.isArray()) {
                Iterator<JsonNode> iterator = resultValue.elements();
                while (iterator!= null && iterator.hasNext()) {
                    JsonNode next = iterator.next();
                    if(null == next){
                        continue;
                    }
                    parseJsonNode(jsonMap, next, fullKey);
                }
                continue;
            }
            if (resultValue.isObject()) {
                parseJsonNode(jsonMap, resultValue, fullKey);
                continue;
            }
            jsonMap.put(fullKey, resultValue.asText());
        }
    }


}
