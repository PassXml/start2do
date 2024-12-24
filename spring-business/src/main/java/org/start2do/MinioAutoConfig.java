package org.start2do;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.start2do.MinioConfig.Item;
import org.start2do.dto.BusinessException;
import org.start2do.util.StringUtils;
import org.start2do.util.spring.SpringBeanUtil;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "start2do.business.minio", value = "enable", havingValue = "true")
@RequiredArgsConstructor
public class MinioAutoConfig {


    private final MinioConfig config;
    private final SpringBeanUtil springBeanUtil;
    private static MinioAutoConfig minioAutoConfig;
    private static ConcurrentHashMap<String, MinioClient> map;
    @Getter
    private static MinioClient defaultMinioClient;
    @Getter
    private static MinioConfig.Item defaultConfig;


    public MinioClient minioClient(MinioConfig.Item config) {
        MinioClient minioClient = MinioClient.builder()
            .endpoint(config.getEndpoint())
            .credentials(config.getAccessKey(), config.getSecretKey())
            .build();
        minioClient.setTimeout(
            config.getConnectTimeout().toMillis(),
            config.getWriteTimeout().toMillis(),
            config.getReadTimeout().toMillis()
        );
        if (config.isCheckBucket()) {
            log.info("Checking if bucket {} exists", config.getBucket());
            BucketExistsArgs existsArgs = BucketExistsArgs.builder()
                .bucket(config.getBucket())
                .build();
            try {
                checkExists(config, minioClient, existsArgs);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new BusinessException("Minio配置错误");
            }
        }
        return minioClient;
    }

    private void checkExists(MinioConfig.Item minioConfig, MinioClient minioClient, BucketExistsArgs existsArgs)
        throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        if (minioClient.bucketExists(existsArgs)) {
            return;
        }
        if (!minioConfig.isCreateBucket()) {
            throw new IllegalStateException("Bucket does not exist: " + minioConfig.getBucket());
        }

        MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder()
            .bucket(minioConfig.getBucket())
            .build();
        minioClient.makeBucket(makeBucketArgs);

        if (!minioConfig.isAnomyousAccess()) {
            return;
        }

        SetBucketPolicyArgs setBucketPolicyArgs = SetBucketPolicyArgs.builder()
            .bucket(minioConfig.getBucket())
            .config(getAnonymousPlocy(minioConfig))
            .build();
        minioClient.setBucketPolicy(setBucketPolicyArgs);
    }

    public String getAnonymousPlocy(MinioConfig.Item minioConfig) {
        String template = "{\"Statement\":[{\"Action\":[\"s3:GetBucketLocation\",\"s3:ListBucket\"],\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Resource\":[\"arn:aws:s3:::%s\"]},{\"Action\":[\"s3:GetObject\"],\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Resource\":[\"arn:aws:s3:::%s/*\"]}],\"Version\":\"2012-10-17\"}";
        return String.format(template, minioConfig.getBucket(),
            minioConfig.getBucket());
    }

    @PostConstruct
    public void init() {
        if (!config.isEnable()) {
            return;
        }
        MinioAutoConfig.minioAutoConfig = this;
        MinioAutoConfig.map = new ConcurrentHashMap<>();
        List<Item> items = config.getItems();
        if (items.isEmpty()) {
            throw new BusinessException("Minio配置不能为空");
        }
        Item item = null;
        if (items.size() == 1) {
            item = items.get(0);
            MinioClient minioClient = minioClient(item);
            MinioAutoConfig.defaultMinioClient = minioClient;
            MinioAutoConfig.defaultConfig = item;
            springBeanUtil.registerBean("minioClient", minioClient);
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            Item t = items.get(i);
            String name = t.getName();
            MinioClient minioClient = minioClient(item);
            if (Boolean.TRUE.equals(t.getIsDefault())) {
                name = "minioClient";
                MinioAutoConfig.defaultMinioClient = minioClient;
                MinioAutoConfig.defaultConfig = t;
            } else {
                if (StringUtils.isEmpty(name)) {
                    name = "miniClient_" + i;
                }
            }
            map.put(name, minioClient);
            springBeanUtil.registerBean(name, minioClient);
        }
    }
}
