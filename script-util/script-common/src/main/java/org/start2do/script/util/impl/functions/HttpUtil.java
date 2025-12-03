package org.start2do.script.util.impl.functions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import okhttp3.CookieJar;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
@UtilityClass
public class HttpUtil {

    private OkHttpClient client = new OkHttpClient.Builder().cookieJar(CookieJar.NO_COOKIES)
        .hostnameVerifier((hostname, session) -> true).build();

    public void reCreateClient(int connectTimeout, int readTimeout, int writeTimeout) {
        client = new OkHttpClient.Builder().cookieJar(CookieJar.NO_COOKIES)
            .connectTimeout(connectTimeout, TimeUnit.SECONDS).readTimeout(readTimeout, TimeUnit.SECONDS)
            .writeTimeout(writeTimeout, TimeUnit.SECONDS).hostnameVerifier((hostname, session) -> true).build();
    }

    public void reCreateClient(OkHttpClient.Builder builder) {
        client = builder.build();
    }

    public Response get(String url) {
        return get(url, null);
    }

    public Response get(String url, Map<String, String> header) {
        Builder builder = new Builder();
        if (header != null) {
            header.forEach(builder::header);
        }
        return executor(builder.get().url(url).build());
    }

    public Response postForm(String url, Map<String, String> header, String data) {
        Builder builder = new Builder();
        if (header != null) {
            header.forEach(builder::header);
        }
        return executor(builder.post(RequestBody.create(Objects.requireNonNullElse(data, ""),
            MediaType.parse("application/x-www-form-urlencoded; charset=utf-8"))).url(url).build());
    }

    public String bodyString(Response response) {
        try {
            return response.body().string();
        } catch (IOException e) {
            log.error("转化body.string()失败,{}", e.getMessage());
        }
        return null;
    }

    public InputStream bodyJsonNode(Response response) {
        return response.body().byteStream();
    }

    private Response executor(Request request) {
        try {
            return client.newCall(request).execute();
        } catch (IOException e) {
            log.error("请求失败:{}", e.getMessage());
        }
        return null;
    }

    public Response postJson(String url, Map<String, String> header, String data) {
        Builder builder = new Builder();
        if (header != null) {
            header.forEach(builder::header);
        }
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        return executor(
            builder.post(RequestBody.create(Objects.requireNonNullElse(data, ""), mediaType)).url(url).build());
    }

    /**
     * 文件下载（不指定保存路径，默认保存到系统临时目录）
     */
    public File downloadFile(String url) {
        return downloadFile(url, null, null);
    }

    /**
     * 文件下载（只自定义 Header，文件保存到系统临时目录）
     */
    public File downloadFile(String url, Map<String, String> header) {
        return downloadFile(url, header, null);
    }

    /**
     * 文件下载
     *
     * @param url          下载地址
     * @param header       自定义请求头
     * @param destFilePath 目标文件路径；如果为空或父目录不存在，则保存到系统临时目录
     * @return 实际保存的文件对象，失败时返回 null
     */
    public File downloadFile(String url, Map<String, String> header, String destFilePath) {
        Builder builder = new Builder();
        if (header != null) {
            header.forEach(builder::header);
        }
        Request request = builder.get().url(url).build();
        Response response = executor(request);
        if (response == null || !response.isSuccessful() || response.body() == null) {
            log.error("文件下载失败,url:{}, code:{}", url, response == null ? null : response.code());
            return null;
        }
        File targetFile = null;
        try (Response res = response; InputStream in = res.body().byteStream()) {
            // 计算实际保存路径
            if (destFilePath == null || destFilePath.isBlank()) {
                targetFile = File.createTempFile("http-download-", ".tmp");
            } else {
                File dest = new File(destFilePath);
                File parent = dest.getParentFile();
                if (parent != null && parent.exists()) {
                    targetFile = dest;
                } else {
                    // 目标目录不存在时，保存到系统临时目录
                    File tmpDir = new File(System.getProperty("java.io.tmpdir"));
                    targetFile = new File(tmpDir, dest.getName());
                }
            }
            try (FileOutputStream out = new FileOutputStream(targetFile)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            }
        } catch (IOException e) {
            log.error("文件下载异常,url:{}, msg:{}", url, e.getMessage());
            return null;
        }
        return targetFile;
    }

    /**
     * 文件上传（仅文件）
     */
    public Response uploadFile(String url, File file) {
        return uploadFile(url, null, null, file, "file");
    }

    /**
     * 文件上传（自定义 Header）
     */
    public Response uploadFile(String url, Map<String, String> header, File file) {
        return uploadFile(url, header, null, file, "file");
    }

    /**
     * 文件上传（自定义 Header + 其他表单参数）
     *
     * @param url           上传地址
     * @param header        自定义请求头
     * @param formParams    其他表单参数
     * @param file          上传文件
     * @param fileFieldName 文件字段名
     * @return 响应对象，失败时返回 null
     */
    public Response uploadFile(String url, Map<String, String> header, Map<String, String> formParams, File file,
        String fileFieldName) {
        if (file == null || !file.exists()) {
            log.error("文件上传失败, 文件不存在");
            return null;
        }
        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart(fileFieldName, file.getName(),
                RequestBody.create(file, MediaType.parse("application/octet-stream")));

        if (formParams != null) {
            formParams.forEach(multipartBuilder::addFormDataPart);
        }

        RequestBody requestBody = multipartBuilder.build();
        Builder builder = new Builder().post(requestBody).url(url);
        if (header != null) {
            header.forEach(builder::header);
        }
        return executor(builder.build());
    }
}
