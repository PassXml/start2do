package org.start2do.default_impl;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
}
