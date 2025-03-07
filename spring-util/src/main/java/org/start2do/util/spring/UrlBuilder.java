package org.start2do.util.spring;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
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
                urlBuilder.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), "UTF-8"))
                    .append("&");
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
        String url = builder.addParam("q", "Java")
            .addParam("page", "2")
            .build();
        System.out.println(
            "Generated URL: " + url);  // Output: Generated URL: https://www.example.com/search?q=Java&page=2

        UrlBuilder builder2 = new UrlBuilder("https://www.example.com/api");
        String url2 = builder2.addParam("param1", "value with spaces")
            .addParam("param2", "anotherValue")
            .build();
        System.out.println("Generated URL 2: "
                           + url2); // Output: Generated URL 2: https://www.example.com/api?param1=value+with+spaces&param2=anotherValue
    }
}
