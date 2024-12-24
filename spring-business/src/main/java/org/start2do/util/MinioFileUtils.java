package org.start2do.util;

import io.minio.DownloadObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.UploadObjectArgs;
import io.minio.messages.Item;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.start2do.dto.BusinessException;

@Slf4j
@UtilityClass
public class MinioFileUtils {


    /**
     * List all objects at root of the bucket
     *
     * @return List of items
     */
    public List<Item> list(MinioClient client, String bucketName, String prefix) {
        ListObjectsArgs args = ListObjectsArgs.builder().bucket(bucketName).prefix(prefix)
            .recursive(false).build();
        Iterable<Result<Item>> myObjects = client.listObjects(args);
        return getItems(myObjects);
    }

    /**
     * List all objects at root of the bucket
     *
     * @return List of items
     */
    public List<Item> fullList(MinioClient client, String bucketName) {
        ListObjectsArgs args = ListObjectsArgs.builder().bucket(bucketName).build();
        Iterable<Result<Item>> myObjects = client.listObjects(args);
        return getItems(myObjects);
    }

    /**
     * List all objects with the prefix given in parameter for the bucket. Simulate a folder hierarchy. Objects within
     * folders (i.e. all objects which match the pattern {@code {prefix}/{objectName}/...}) are not returned
     *
     * @param path Prefix of seeked list of object
     * @return List of items
     */
    public List<Item> list(MinioClient client, String bucketName, Path path) {
        ListObjectsArgs args = ListObjectsArgs.builder().bucket(bucketName)
            .prefix(path.toString()).recursive(false).build();
        Iterable<Result<Item>> myObjects = client.listObjects(args);
        return getItems(myObjects);
    }

    public List<Item> getFullList(MinioClient client, String bucketName, String prefix) {
        ListObjectsArgs args = ListObjectsArgs.builder().bucket(bucketName)
            .prefix(prefix).build();
        Iterable<Result<Item>> myObjects = client.listObjects(args);
        return getItems(myObjects);
    }

    /**
     * Utility method which map results to items and return a list
     *
     * @param myObjects Iterable of results
     * @return List of items
     */
    private List<Item> getItems(Iterable<Result<Item>> myObjects) {
        return StreamSupport.stream(myObjects.spliterator(), true).map(itemResult -> {
            try {
                return itemResult.get();
            } catch (Exception e) {
                log.info(e.getMessage(), e);
                throw new BusinessException("Error while parsing list of objects");
            }
        }).collect(Collectors.toList());
    }

    /**
     * Get an object from Minio
     */
    public InputStream get(MinioClient client, String bucketName, Path path) {
        try {
            GetObjectArgs args = GetObjectArgs.builder().bucket(bucketName)
                .object(path.toString()).build();
            return client.getObject(args);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException("Error while fetching files in Minio");
        }
    }

    /**
     * Get metadata of an object from Minio
     */
    public StatObjectResponse getMetadata(MinioClient client, String bucketName, Path path) {
        try {
            StatObjectArgs args = StatObjectArgs.builder().bucket(bucketName)
                .object(path.toString()).build();
            return client.statObject(args);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException("Error while fetching files in Minio");
        }
    }

    /**
     * Get metadata for multiples objects from Minio
     *
     * @param paths Paths of all objects with prefix. Objects names must be included.
     * @return A map where all paths are keys and metadatas are values
     */
    public Map<Path, StatObjectResponse> getMetadata(MinioClient client, String bucketName, Iterable<Path> paths) {
        return StreamSupport.stream(paths.spliterator(), false).map(path -> {
            try {
                StatObjectArgs args = StatObjectArgs.builder()
                    .bucket(bucketName).object(path.toString()).build();
                return new HashMap.SimpleEntry<>(path, client.statObject(args));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new BusinessException(e.getMessage());
            }
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void getAndSave(MinioClient client, String bucketName, String path, String fileName) {
        try {
            DownloadObjectArgs args = DownloadObjectArgs.builder().bucket(bucketName)
                .object(path).filename(fileName).build();
            client.downloadObject(args);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException("Error while fetching files in Minio");
        }
    }

    /**
     * Upload a file to Minio
     */
    public void upload(MinioClient client, String bucketName, String path, InputStream file,
        Map<String, String> headers) {
        try {
            PutObjectArgs args = PutObjectArgs.builder().bucket(bucketName).object(path)
                .stream(file, file.available(), -1).headers(headers).build();
            client.putObject(args);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("上传失败");
        }
    }

    /**
     * Upload a file to Minio
     *
     * @param path Path with prefix to the object. Object name must be included.
     * @param file File as an inputstream
     */
    public void upload(MinioClient client, String bucketName, String path, InputStream file) {
        try {
            PutObjectArgs args = PutObjectArgs.builder().bucket(bucketName).object(path)
                .stream(file, file.available(), -1).build();
            client.putObject(args);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException("上传失败");
        }
    }

    public StatObjectResponse uploadReturnMeta(MinioClient client, String bucketName, String path, InputStream file) {
        try {
            PutObjectArgs args = PutObjectArgs.builder().bucket(bucketName).object(path)
                .stream(file, file.available(), -1).build();
            client.putObject(args);
            return getMetadata(client, bucketName, Path.of(path));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException("Error while fetching files in Minio");
        }
    }

    /**
     * Upload a file to Minio
     *
     * @param path        Path with prefix to the object. Object name must be included.
     * @param file        File as an inputstream
     * @param contentType MIME type for the object
     * @param headers     Additional headers to put on the file. The map MUST be mutable
     */
    public void upload(MinioClient client, String bucketName, String path, InputStream file, String contentType,
        Map<String, String> headers) {
        try {
            PutObjectArgs args = PutObjectArgs.builder().bucket(bucketName).object(path)
                .stream(file, file.available(), -1).headers(headers).contentType(contentType).build();

            client.putObject(args);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException("上传失败");
        }
    }

    /**
     * Upload a file to Minio
     *
     * @param path        Path with prefix to the object. Object name must be included.
     * @param file        File as an inputstream
     * @param contentType MIME type for the object
     */
    public void upload(MinioClient client, String bucketName, String path, InputStream file, String contentType) {
        try {
            PutObjectArgs args = PutObjectArgs.builder().bucket(bucketName).object(path)
                .stream(file, file.available(), -1).contentType(contentType).build();
            client.putObject(args);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException("Error while fetching files in Minio");
        }
    }

    public void upload(MinioClient client, String bucketName, String path, URL url) {
        try (InputStream inputStream = url.openStream()) {
            ObjectWriteResponse response = client.putObject(
                PutObjectArgs.builder().bucket(bucketName).object(path)
                    .stream(inputStream, -1, 1024 * 1024 * 5).build());
            log.debug("Url上传结果:{}", response.object());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException("Error while fetching files in Minio");
        }
    }

    /**
     * Upload a file to Minio upload file bigger than Xmx size
     *
     * @param path Path with prefix to the object. Object name must be included.
     * @param file File as an Filename
     */
    public void upload(MinioClient client, String bucketName, String path, File file) {
        try {
            UploadObjectArgs args = UploadObjectArgs.builder().bucket(bucketName)
                .object(path).filename(file.getAbsolutePath()).build();
            client.uploadObject(args);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException("上传失败");
        }
    }


    /**
     * Remove a file to Minio
     *
     * @param path Path with prefix to the object. Object name must be included.
     */
    public void remove(MinioClient client, String bucketName, String path) {
        try {
            RemoveObjectArgs args = RemoveObjectArgs.builder().bucket(bucketName)
                .object(path).build();
            client.removeObject(args);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException("Error while fetching files in Minio");
        }
    }

    /**
     * 获取所有文件大小
     */
    public long getBucketSize(MinioClient client, String bucketName) {
        Iterable<Result<Item>> results = client.listObjects(
            io.minio.ListObjectsArgs.builder().bucket(bucketName).build());
        long totalSize = 0;
        for (Result<Item> result : results) {
            Item item = null;
            try {
                item = result.get();
                totalSize += item.size();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return totalSize;
    }
}
