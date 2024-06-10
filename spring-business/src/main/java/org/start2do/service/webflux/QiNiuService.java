package org.start2do.service.webflux;


import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.stereotype.Service;
import org.start2do.config.QiNiuConfig;
import org.start2do.util.JsonUtil;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "start2do.business.file-setting", name = "type", havingValue = "qn")
@ConditionalOnWebApplication(type = Type.REACTIVE)
public class QiNiuService implements CommandLineRunner {

    private Auth auth;
    private final QiNiuConfig config;
    private UploadManager uploadManager;
    private BucketManager bucketManager;

    @Override
    public void run(String... args) {
        auth = Auth.create(config.getAccessKey(), config.getSecretKey());
        Configuration cfg = new Configuration(Region.autoRegion());
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;
        uploadManager = new UploadManager(cfg);
        bucketManager = new BucketManager(auth, cfg);

    }

    public DefaultPutRet upload(byte[] bytes, String key) {
        String upToken = auth.uploadToken(config.getBucket());
        try {
            Response response = uploadManager.put(bytes, String.format("%s/%s", config.getBaseDir(), key), upToken);
            //解析上传成功的结果
            return JsonUtil.toObject(response.bodyString(), DefaultPutRet.class);
        } catch (QiniuException ex) {
            ex.printStackTrace();
            if (ex.response != null) {
                log.error("上传七牛失败：{}", ex.response);
            }
        }
        return null;
    }

    public void delete(String key) {
        try {
            bucketManager.delete(config.getBucket(), key);
        } catch (QiniuException e) {
            throw new RuntimeException(e);
        }
    }

    public void move(String key, String toKey) {
        try {
            bucketManager.move(config.getBucket(), key, config.getBucket(), toKey);
        } catch (QiniuException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
