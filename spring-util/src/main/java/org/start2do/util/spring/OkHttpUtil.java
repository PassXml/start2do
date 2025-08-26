package org.start2do.util.spring;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
public class OkHttpUtil {

    private static final OkHttpClient client;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType FORM_URL_ENCODED = MediaType.parse(
        "application/x-www-form-urlencoded; charset=utf-8");

    static {
        // 连接超时时间
        client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
            // 读取超时时间
            .readTimeout(10, TimeUnit.SECONDS)
            // 写入超时时间
            .writeTimeout(10, TimeUnit.SECONDS)
            .build();
    }

    public static OkHttpClient getClient() {
        return client;
    }

    /**
     * 同步 GET 请求
     */
    public static String syncGet(String url) throws IOException {
        return syncGet(url, null);
    }

    /**
     * 同步 GET 请求（带请求头）
     */
    public static String syncGet(String url, Map<String, String> headers) throws IOException {
        Request.Builder requestBuilder = new Request.Builder().url(url);

        // 添加请求头
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        return executor(requestBuilder.build());
    }

    /**
     * 异步 GET 请求
     */
    public static void asyncGet(String url, okhttp3.Callback callback) {
        asyncGet(url, null, callback);
    }

    /**
     * 异步 GET 请求（带请求头）
     */
    public static void asyncGet(String url, Map<String, String> headers, okhttp3.Callback callback) {
        Request.Builder requestBuilder = new Request.Builder().url(url);

        // 添加请求头
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        Request request = requestBuilder.build();

        client.newCall(request).enqueue(callback);
    }

    /**
     * 同步 POST 请求
     */
    public static String syncPost(String url, Map<String, String> params) throws IOException {
        return syncPost(url, params, null);
    }

    /**
     * 同步 POST 请求（带请求头）
     */
    public static String syncPost(String url, Map<String, String> params, Map<String, String> headers)
        throws IOException {
        FormBody.Builder builder = new FormBody.Builder();
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }

        RequestBody formBody = builder.build();
        Request.Builder requestBuilder = new Request.Builder().url(url).post(formBody);

        // 添加请求头
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        return executor(requestBuilder.build());
    }

    /**
     * 同步 POST 请求（使用 FormBody.Builder 构建表单）
     */
    public static String syncPost(String url, FormBody.Builder formBuilder) throws IOException {
        return syncPost(url, formBuilder, null);
    }

    /**
     * 同步 POST 请求（使用 FormBody.Builder 构建表单，带请求头）
     */
    public static String syncPost(String url, FormBody.Builder formBuilder, Map<String, String> headers)
        throws IOException {
        RequestBody formBody = formBuilder.build();
        Request.Builder requestBuilder = new Request.Builder().url(url).post(formBody);

        // 添加请求头
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        return executor(requestBuilder.build());
    }

    /**
     * 异步 POST 请求
     */
    public static void asyncPost(String url, Map<String, String> params, okhttp3.Callback callback) {
        asyncPost(url, params, null, callback);
    }

    /**
     * 异步 POST 请求（带请求头）
     */
    public static void asyncPost(String url, Map<String, String> params, Map<String, String> headers,
        okhttp3.Callback callback) {
        FormBody.Builder builder = new FormBody.Builder();
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }

        RequestBody formBody = builder.build();
        Request.Builder requestBuilder = new Request.Builder().url(url).post(formBody);

        // 添加请求头
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        Request request = requestBuilder.build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * 异步 POST 请求（使用 FormBody.Builder 构建表单）
     */
    public static void asyncPost(String url, FormBody.Builder formBuilder, okhttp3.Callback callback) {
        asyncPost(url, formBuilder, null, callback);
    }

    /**
     * 异步 POST 请求（使用 FormBody.Builder 构建表单，带请求头）
     */
    public static void asyncPost(String url, FormBody.Builder formBuilder, Map<String, String> headers,
        okhttp3.Callback callback) {
        RequestBody formBody = formBuilder.build();
        Request.Builder requestBuilder = new Request.Builder().url(url).post(formBody);

        // 添加请求头
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        Request request = requestBuilder.build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * 同步 POST 请求（发送表单数据，x-www-form-urlencoded 格式）
     */
    public static String syncPostForm(String url, String formData) throws IOException {
        return syncPostForm(url, formData, null);
    }

    /**
     * 同步 POST 请求（发送表单数据，x-www-form-urlencoded 格式，带请求头）
     */
    public static String syncPostForm(String url, String formData, Map<String, String> headers) throws IOException {
        RequestBody requestBody = RequestBody.create(formData, FORM_URL_ENCODED);

        Request.Builder requestBuilder = new Request.Builder().url(url).post(requestBody);

        // 添加请求头
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            } else {
                throw new IOException("Unexpected code " + response);
            }
        }
    }

    /**
     * 异步 POST 请求（发送表单数据，x-www-form-urlencoded 格式）
     */
    public static void asyncPostForm(String url, String formData, okhttp3.Callback callback) {
        asyncPostForm(url, formData, null, callback);
    }

    /**
     * 异步 POST 请求（发送表单数据，x-www-form-urlencoded 格式，带请求头）
     */
    public static void asyncPostForm(String url, String formData, Map<String, String> headers,
        okhttp3.Callback callback) {
        RequestBody requestBody = RequestBody.create(formData, FORM_URL_ENCODED);

        Request.Builder requestBuilder = new Request.Builder().url(url).post(requestBody);

        // 添加请求头
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        Request request = requestBuilder.build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * 文件下载
     */
    public static void downloadFile(String url, String destFilePath) throws IOException {
        downloadFile(url, destFilePath, null);
    }

    /**
     * 文件下载（带请求头）
     */
    public static void downloadFile(String url, String destFilePath, Map<String, String> headers) throws IOException {
        Request.Builder requestBuilder = new Request.Builder().url(url);

        // 添加请求头
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                InputStream inputStream = null;
                FileOutputStream outputStream = null;
                try {
                    inputStream = response.body().byteStream();
                    File file = new File(destFilePath);
                    outputStream = new FileOutputStream(file);

                    byte[] buffer = new byte[2048];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                    }
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                }
            } else {
                throw new IOException("Unexpected code " + response);
            }
        }
    }

    /**
     * 文件上传
     */
    public static String uploadFile(String url, File file) throws IOException {
        return uploadFile(url, file, null);
    }

    /**
     * 文件上传（带请求头）
     */
    public static String uploadFile(String url, File file, Map<String, String> headers) throws IOException {
        return uploadFile("POST", url, file, headers);
    }

    /**
     * 文件上传（带请求头）
     */
    public static String uploadFile(String method, String url, File file, Map<String, String> headers)
        throws IOException {
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("file", file.getName(),
                RequestBody.create(file, MediaType.parse("application/octet-stream"))).build();
        Request.Builder requestBuilder = new Request.Builder().url(url).method(method, requestBody);
        // 添加请求头
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        return executor(requestBuilder.build());
    }

    public static String executor(Request request) throws IOException {
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            } else {
                throw new IOException("Unexpected code " + response);
            }
        }
    }

    /**
     * 文件上传（带请求头）
     */
    public static String uploadFile(String method, String url, String fileName, byte[] file,
        Map<String, String> headers) throws IOException {
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("file", fileName, RequestBody.create(file, MediaType.parse("application/octet-stream")))
            .build();

        Request.Builder requestBuilder = new Request.Builder().url(url).method(method, requestBody);

        // 添加请求头
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        return executor(requestBuilder.build());
    }

    /**
     * 文件上传（带表单参数）
     */
    public static String uploadFileWithForm(String url, File file, Map<String, String> formParams) throws IOException {
        return uploadFileWithForm(url, file, formParams, null);
    }

    /**
     * 文件上传（带表单参数和请求头）
     */
    public static String uploadFileWithForm(String url, File file, Map<String, String> formParams,
        Map<String, String> headers) throws IOException {
        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("file", file.getName(),
                RequestBody.create(file, MediaType.parse("application/octet-stream")));

        // 添加表单参数
        if (formParams != null && !formParams.isEmpty()) {
            for (Map.Entry<String, String> entry : formParams.entrySet()) {
                multipartBuilder.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }

        RequestBody requestBody = multipartBuilder.build();
        Request.Builder requestBuilder = new Request.Builder().url(url).post(requestBody);

        // 添加请求头
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        return executor(requestBuilder.build());
    }

    /**
     * 同步 POST 请求（发送JSON数据）
     */
    public static String syncPostJson(String url, String jsonBody) throws IOException {
        return syncPostJson(url, jsonBody, null);
    }

    /**
     * 同步 POST 请求（发送JSON数据，带请求头）
     */
    public static String syncPostJson(String url, String jsonBody, Map<String, String> headers) throws IOException {
        RequestBody requestBody = RequestBody.create(jsonBody, JSON);

        Request.Builder requestBuilder = new Request.Builder().url(url).post(requestBody);

        // 添加请求头
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        return executor(requestBuilder.build());
    }

    /**
     * 异步 POST 请求（发送JSON数据）
     */
    public static void asyncPostJson(String url, String jsonBody, okhttp3.Callback callback) {
        asyncPostJson(url, jsonBody, null, callback);
    }

    /**
     * 异步 POST 请求（发送JSON数据，带请求头）
     */
    public static void asyncPostJson(String url, String jsonBody, Map<String, String> headers,
        okhttp3.Callback callback) {
        RequestBody requestBody = RequestBody.create(jsonBody, JSON);

        Request.Builder requestBuilder = new Request.Builder().url(url).post(requestBody);

        // 添加请求头
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        Request request = requestBuilder.build();
        client.newCall(request).enqueue(callback);
    }
}
