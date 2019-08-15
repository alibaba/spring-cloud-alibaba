package com.alibaba.cloud.nacos.parser;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ByteArrayResource;

import java.util.Properties;

/**
 * @Author: zkz
 */
public class NacosDataYamlParser extends AbstractNacosDataParser {

    public NacosDataYamlParser() {
        super(",yml,yaml,");
    }

    @Override
    protected Properties doParse(String data) {
        YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
        yamlFactory.setResources(new ByteArrayResource(data.getBytes()));
        return yamlFactory.getObject();
    }
}
