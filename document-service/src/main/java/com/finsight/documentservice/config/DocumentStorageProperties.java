package com.finsight.documentservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "document")
public class DocumentStorageProperties {
    /**
     * Directory where uploaded PDF files are stored.
     */
    private String uploadDir = System.getProperty("user.home") + "/finsight/uploads";

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }
}
