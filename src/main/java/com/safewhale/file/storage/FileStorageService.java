package com.safewhale.file.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    StoredFile upload(MultipartFile file);

    /** 업로드된 파일을 다시 읽는다. AI 서버에 사진을 base64 로 넘길 때 쓴다. */
    LoadedFile load(String url);

    record StoredFile(String url, String originalFilename, String mimeType, long sizeBytes) {}
    record LoadedFile(byte[] bytes, String mimeType) {}
}
