package com.alibaba.alicloud.oss.resource;

import com.aliyun.oss.model.Bucket;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lich
 * @date 2019/8/30
 */
public class DummyOssClient {

    private Map<String, byte[]> storeMap = new ConcurrentHashMap<>();

    private Map<String, Bucket> bucketSet = new HashMap<>();

    public String getStoreKey(String bucketName, String objectKey) {
        return String.join(".", bucketName, objectKey);
    }

    public PutObjectResult putObject(String bucketName, String objectKey, InputStream inputStream) {

        try {
            byte[] result = StreamUtils.copyToByteArray(inputStream);
            storeMap.put(getStoreKey(bucketName, objectKey), result);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                inputStream.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }


        return new PutObjectResult();
    }

    public OSSObject getOSSObject(String bucketName, String objectKey) {
        byte[] value = storeMap.get(this.getStoreKey(bucketName, objectKey));
        if (value == null) {
            return null;
        }
        OSSObject ossObject = new OSSObject();
        ossObject.setBucketName(bucketName);
        ossObject.setKey(objectKey);
        InputStream inputStream = new ByteArrayInputStream(value);
        ossObject.setObjectContent(inputStream);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(value.length);
        ossObject.setObjectMetadata(objectMetadata);

        return ossObject;
    }

    public Bucket createBucket(String bucketName) {
        if (bucketSet.containsKey(bucketName)) {
            return bucketSet.get(bucketName);
        }
        Bucket bucket = new Bucket();
        bucket.setCreationDate(new Date());
        bucket.setName(bucketName);
        bucketSet.put(bucketName, bucket);
        return bucket;
    }

    public List<Bucket> bucketList() {
        return new ArrayList<>(bucketSet.values());
    }
}
