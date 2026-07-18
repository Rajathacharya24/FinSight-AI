package com.finsight.documentservice.service;

import org.springframework.web.multipart.MultipartFile;

public interface VirusScannerService {
    /**
     * Scans a file for viruses/malware.
     * @param file the file to scan
     * @return true if the file is clean, false if infected or scan fails
     */
    boolean scanFile(MultipartFile file);
}
