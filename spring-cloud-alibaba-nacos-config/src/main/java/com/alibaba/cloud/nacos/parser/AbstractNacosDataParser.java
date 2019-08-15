package com.alibaba.cloud.nacos.parser;

import com.alibaba.nacos.client.utils.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @Author: zkz
 */
public abstract class AbstractNacosDataParser {

    protected static final String DOT = ".";
    protected static final String VALUE = "value";

    private String extension;
    private AbstractNacosDataParser nextParser;

    protected AbstractNacosDataParser(String extension) {
        if (StringUtils.isEmpty(extension)) {
            throw new IllegalArgumentException("extension cannot be empty");
        }
        this.extension = extension.toLowerCase();
    }
    /** Verify file extensions */
    public final boolean checkFileExtension(String extension) {
        if (this.isLegal(extension.toLowerCase())) {
            return true;
        }
        if (this.nextParser == null) {
            return false;
        }
        return this.nextParser.checkFileExtension(extension);

    }

    /** Parsing nacos configuration content */
    public final Properties parseNacosData(String data, String extension) throws IOException {
        if (extension == null || extension.length() < 1) {
            throw new IllegalStateException("The file extension cannot be empty");
        }
        if (this.isLegal(extension.toLowerCase())) {
            return this.doParse(data);
        }
        if (this.nextParser == null) {
            throw new IllegalStateException(getTips(extension));
        }
        return this.nextParser.parseNacosData(data, extension);
    }

    /** Core logic for parsing */
    protected abstract Properties doParse(String data) throws IOException;

    protected AbstractNacosDataParser setNextParser(AbstractNacosDataParser nextParser) {
        this.nextParser = nextParser;
        return this;
    }

    /** add the next parser */
    public AbstractNacosDataParser addNextParser(AbstractNacosDataParser nextParser) {
        if (this.nextParser == null) {
            this.nextParser = nextParser;
        } else {
            this.nextParser.addNextParser(nextParser);
        }
        return this;
    }

    protected boolean isLegal(String extension) {
        return this.extension.equalsIgnoreCase(extension) || this.extension.contains(extension);
    }


    /**
     * Generate key-value pairs from the map
     */
    protected Properties generateProperties(Map<String, String> map) {
        if (null == map || map.isEmpty()) {
            return null;
        }
        Properties properties = new Properties();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            if (StringUtils.isBlank(key)) {
                continue;
            }
            key = key.startsWith(DOT) ? key.replaceFirst("\\.", "") : key;
            properties.put(key, entry.getValue());
        }
        return properties;
    }

    /**
     * Reload the key ending in `value`,if you need
     */
    protected Map<String,String> reloadMap(Map<String,String> map){
        if (map == null || map.isEmpty()) {
            return null;
        }
        Map<String, String> result = new HashMap<>(map);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            if (key.contains(DOT)) {
                int idx = key.lastIndexOf(DOT);
                String suffix = key.substring(idx + 1);
                if (VALUE.equalsIgnoreCase(suffix)) {
                    result.put(key.substring(0, idx), entry.getValue());
                }
            }
        }
        return result;
    }


    public static String getTips(String fileName) {
        return String.format("[%s] must contains file extension with properties|yaml|yml|xml|json", fileName);
    }

}
