package org.start2do.util.spring;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UrlBuilder {

    private final String baseUrl;
    private final Map<String, String> params = new HashMap<>();

    public UrlBuilder(String baseUrl) {
        Objects.requireNonNull(baseUrl, "Base URL cannot be null");
        this.baseUrl = baseUrl;
    }

    public UrlBuilder addParam(String name, String value) {
        Objects.requireNonNull(name, "Parameter name cannot be null");
        Objects.requireNonNull(value, "Parameter value cannot be null");
        this.params.put(name, value);
        return this;
    }

    public String build() {
        if (params.isEmpty()) {
            return baseUrl;
        }

        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        if (!baseUrl.contains("?")) {
            urlBuilder.append("?");
        } else if (!baseUrl.endsWith("&")) {
            urlBuilder.append("&");
        }

        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                urlBuilder.append(URLEncoder.encode(entry.getKey(), "UTF-8")).append("=")
                    .append(URLEncoder.encode(entry.getValue(), "UTF-8")).append("&");
            }
        } catch (UnsupportedEncodingException e) {
            // This should not happen, since UTF-8 is always supported.
            throw new RuntimeException("UTF-8 encoding is not supported", e);
        }

        // Remove the trailing "&"
        if (urlBuilder.charAt(urlBuilder.length() - 1) == '&') {
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        }

        return urlBuilder.toString();
    }

    public static void main(String[] args) {
        // Example usage:
        UrlBuilder builder = new UrlBuilder("https://www.example.com/search");
        String url = builder.addParam("q", "Java").addParam("page", "2").build();
        System.out.println(
            "Generated URL: " + url);  // Output: Generated URL: https://www.example.com/search?q=Java&page=2

        UrlBuilder builder2 = new UrlBuilder("https://www.example.com/api");
        String url2 = builder2.addParam("param1", "value with spaces").addParam("param2", "anotherValue").build();
        System.out.println("Generated URL 2: "
                           + url2); // Output: Generated URL 2: https://www.example.com/api?param1=value+with+spaces&param2=anotherValue
        // 测试新的参数提取方法
        String testUrl = "https://www.example.com/search?q=Java&tag=spring&tag=boot&page=1";
        String qValue = extractParam(testUrl, "q");
        String tagValue = extractParam(testUrl, "tag");
        String nonExistentParam = extractParam(testUrl, "notexist");

        System.out.println("\n测试参数提取:");
        System.out.println("q parameter: " + qValue);  // 输出: Java
        System.out.println("First tag: " + tagValue);  // 输出: spring
        System.out.println("Non-existent parameter: " + nonExistentParam);  //
    }

    public static String extractParam(String url, String paramName) {
        Objects.requireNonNull(url, "URL cannot be null");
        Objects.requireNonNull(paramName, "Parameter name cannot be null");

        Map<String, List<String>> params = extractParams(url);
        List<String> values = params.get(paramName);
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }

    public static Map<String, List<String>> extractParams(String url) {
        Objects.requireNonNull(url, "URL cannot be null");
        Map<String, List<String>> params = new HashMap<>();

        int queryIndex = url.indexOf('?');
        if (queryIndex == -1) {
            return params;
        }

        String queryString = url.substring(queryIndex + 1);
        String[] pairs = queryString.split("&");

        for (String pair : pairs) {
            int equalIndex = pair.indexOf('=');
            if (equalIndex > 0) {
                try {
                    String key = java.net.URLDecoder.decode(pair.substring(0, equalIndex), "UTF-8");
                    String value = java.net.URLDecoder.decode(pair.substring(equalIndex + 1), "UTF-8");
                    params.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("UTF-8 decoding failed", e);
                }
            }
        }

        return params;
    }
}
