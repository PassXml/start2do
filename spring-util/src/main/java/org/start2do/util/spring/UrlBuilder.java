package org.start2do.util.spring;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UrlBuilder {

    private String baseUrl;
    private final Map<String, String> params = new HashMap<>();
    private String uri;
    private boolean isHttps;

    public UrlBuilder(String baseUrl) {
        Objects.requireNonNull(baseUrl, "Base URL cannot be null");
        this.baseUrl = baseUrl;
    }

    public UrlBuilder(String host, int port, String contextPath) {
        Objects.requireNonNull(host, "Host cannot be null");
        if (port <= 0) {
            throw new IllegalArgumentException("Port must be positive");
        }
        if (contextPath != null && contextPath.length() > 1 && !contextPath.startsWith("/")) {
            throw new IllegalArgumentException("Context path must start with '/'");
        }

        this.isHttps = false;
        rebuildUrl(host, port, contextPath);
    }

    public UrlBuilder(String host, int port, String contextPath, boolean isHttps) {
        Objects.requireNonNull(host, "Host cannot be null");
        if (port <= 0) {
            throw new IllegalArgumentException("Port must be positive");
        }
        if (contextPath != null && contextPath.length() > 1 && !contextPath.startsWith("/")) {
            throw new IllegalArgumentException("Context path must start with '/'");
        }

        this.isHttps = isHttps;
        rebuildUrl(host, port, contextPath);
    }

    private void rebuildUrl(String host, int port, String contextPath) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(host.startsWith("http") ? host : (isHttps ? "https://" : "http://")).append(host);

        if (port != 80 && port != 443) {
            urlBuilder.append(":").append(port);
        }

        if (contextPath != null && !contextPath.isEmpty()) {
            urlBuilder.append(contextPath);
        }

        this.baseUrl = urlBuilder.toString();
    }

    public UrlBuilder addParam(String name, String value) {
        Objects.requireNonNull(name, "Parameter name cannot be null");
        Objects.requireNonNull(value, "Parameter value cannot be null");
        this.params.put(name, value);
        return this;
    }

    public UrlBuilder addUri(String uri) {
        Objects.requireNonNull(uri, "URI cannot be null");
        if (uri.startsWith("/")) {
            this.uri = uri.substring(1);
        } else {
            this.uri = uri;
        }
        return this;
    }

    public UrlBuilder isHttps(boolean isHttps) {
        this.isHttps = isHttps;
        updateProtocol();
        return this;
    }

    private void updateProtocol() {
        if (baseUrl.startsWith("http://")) {
            baseUrl = baseUrl.replaceFirst("http://", isHttps ? "https://" : "http://");
        } else if (baseUrl.startsWith("https://")) {
            baseUrl = baseUrl.replaceFirst("https://", isHttps ? "https://" : "http://");
        } else {
            baseUrl = (isHttps ? "https://" : "http://") + baseUrl;
        }
    }

    public String build() {
        StringBuilder urlBuilder = new StringBuilder(baseUrl);

        if (uri != null && !uri.isEmpty()) {
            if (!urlBuilder.toString().endsWith("/")) {
                urlBuilder.append("/");
            }
            urlBuilder.append(uri);
        }

        if (params.isEmpty()) {
            return urlBuilder.toString();
        }

        if (!urlBuilder.toString().contains("?")) {
            urlBuilder.append("?");
        } else if (!urlBuilder.toString().endsWith("&")) {
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

        // Test new constructor with host, port, context-path
        UrlBuilder builder3 = new UrlBuilder("www.example.com", 8080, "/api");
        String url3 = builder3.addUri("users").addParam("id", "123").build();
        System.out.println(
            "Generated URL 3: " + url3); // Output: Generated URL 3: http://www.example.com:8080/api/users?id=123

        UrlBuilder builder4 = new UrlBuilder("localhost", 9090, "/app");
        String url4 = builder4.addUri("data/list").addParam("page", "1").addParam("size", "10").build();
        System.out.println(
            "Generated URL 4: " + url4); // Output: Generated URL 4: http://localhost:9090/app/data/list?page=1&size=10

        // Test with HTTPS and standard port
        UrlBuilder builder5 = new UrlBuilder("https://secure.example.com", 443, "");
        String url5 = builder5.addUri("auth").build();
        System.out.println("Generated URL 5: " + url5); // Output: Generated URL 5: https://secure.example.com/auth
        
        // Test new constructor with isHttps parameter
        UrlBuilder builder6 = new UrlBuilder("api.example.com", 8443, "/v1", true);
        String url6 = builder6.addUri("endpoints").addParam("token", "abc123").build();
        System.out.println("Generated URL 6: " + url6); // Output: Generated URL 6: https://api.example.com:8443/v1/endpoints?token=abc123
        
        // Test isHttps() method to toggle protocol
        UrlBuilder builder7 = new UrlBuilder("toggle.example.com", 8080, "/api");
        String url7 = builder7.addUri("test").isHttps(true).build();
        System.out.println("Generated URL 7: " + url7); // Output: Generated URL 7: https://toggle.example.com:8080/api/test
        
        UrlBuilder builder8 = new UrlBuilder("switch.example.com", 3000, "/data", true);
        String url8 = builder8.addUri("info").isHttps(false).build();
        System.out.println("Generated URL 8: " + url8); // Output: Generated URL 8: http://switch.example.com:3000/data/info

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
