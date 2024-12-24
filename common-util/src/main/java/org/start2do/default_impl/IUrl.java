package org.start2do.default_impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.StringJoiner;

/**
 * `IUrl` is an interface for URL. It provides methods to get host, uri, and full URL.
 */
public interface IUrl {

    @JsonIgnore
    String host();

    @JsonIgnore
    String uri();

    default String getUrl() {
        if (host() == null || uri() == null) {
            return "";
        }
        if (host().endsWith("/") || uri().startsWith("/")) {
            return host() + uri();
        } else {
            return host() + "/" + uri();
        }
    }

    static String getUrl(String host, String uri) {
        if (host == null || uri == null) {
            return "";
        }
        if (host.endsWith("/") || uri.startsWith("/")) {
            return host + uri;
        } else {
            return host + "/" + uri;
        }
    }

    static String getUrl(CharSequence delimiter, String... uri) {
        String delimiterStr = delimiter.toString();
        StringJoiner joiner = new StringJoiner("");
        for (String s : uri) {
            if (!s.startsWith(delimiterStr)) {
                joiner.add(delimiterStr);
            }
            if (s.endsWith(delimiterStr)) {
                joiner.add(s.substring(0, s.length() - 1));
            } else {
                joiner.add(s);
            }
        }
        return joiner.toString();
    }
}
