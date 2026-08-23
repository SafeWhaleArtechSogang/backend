package com.safewhale.file.storage;

import com.safewhale.common.exception.BusinessException;
import com.safewhale.common.exception.ErrorCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalFileStorageService implements FileStorageService {
    private final Path uploadDirectory;

    public LocalFileStorageService(@Value("${app.file.upload-dir}") String uploadDirectory) {
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    @Override
    public StoredFile upload(MultipartFile file) {
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());
        String extension = original.lastIndexOf('.') >= 0 ? original.substring(original.lastIndexOf('.')) : "";
        String storedName = UUID.randomUUID() + extension;
        try {
            Files.createDirectories(uploadDirectory);
            Files.copy(file.getInputStream(), uploadDirectory.resolve(storedName), StandardCopyOption.REPLACE_EXISTING);
            return new StoredFile("/files/" + storedName, original, file.getContentType(), file.getSize());
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.FILE_STORAGE_ERROR);
        }
    }
}
