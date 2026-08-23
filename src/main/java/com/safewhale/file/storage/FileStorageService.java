package com.safewhale.file.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    StoredFile upload(MultipartFile file);
    record StoredFile(String url, String originalFilename, String mimeType, long sizeBytes) {}
}
