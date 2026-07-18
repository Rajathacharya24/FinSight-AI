package com.finsight.documentservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class ClamAvScannerStub implements VirusScannerService {

    @Override
    public boolean scanFile(MultipartFile file) {
        log.info("Scanning file {} for malware using ClamAV stub...", file.getOriginalFilename());
        // For MVP, assume all files are clean unless filename contains "eicar"
        if (file.getOriginalFilename() != null && file.getOriginalFilename().toLowerCase().contains("eicar")) {
            log.warn("Malware detected in file {}!", file.getOriginalFilename());
            return false;
        }
        log.info("File {} is clean.", file.getOriginalFilename());
        return true;
    }
}
