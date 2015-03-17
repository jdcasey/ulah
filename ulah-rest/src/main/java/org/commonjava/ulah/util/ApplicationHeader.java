package org.commonjava.ulah.util;

public enum ApplicationHeader {
    content_type("Content-Type"), content_length("Content-Length"), last_modified(
            "Last-Modified");

    private String key;

    private ApplicationHeader(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

}
