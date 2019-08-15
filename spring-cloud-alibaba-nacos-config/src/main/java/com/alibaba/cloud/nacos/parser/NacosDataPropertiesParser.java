package com.alibaba.cloud.nacos.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

/**
 * @Author: zkz
 */
public class NacosDataPropertiesParser extends AbstractNacosDataParser {


    public NacosDataPropertiesParser() {
        super("properties");
    }

    @Override
    protected Properties doParse(String data) throws IOException {
        Properties properties = new Properties();
        properties.load(new StringReader(data));
        return properties;
    }
}
