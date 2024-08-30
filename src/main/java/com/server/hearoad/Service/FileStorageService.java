package com.server.hearoad.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation = Paths.get("path/to/store/images").toAbsolutePath().normalize();

    public FileStorageService() {
        try {
            Files.createDirectories(fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("업로드된 파일을 저장할 디렉토리를 생성할 수 없습니다.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        // 파일 이름을 고유하게 만듦
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        try {
            // 파일을 지정된 위치에 저장 (동일한 이름의 기존 파일 대체)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 파일의 URL 또는 경로를 반환
            return "/images/" + fileName;
        } catch (Exception ex) {
            throw new RuntimeException("파일 " + fileName + "을(를) 저장할 수 없습니다. 다시 시도해주세요!", ex);
        }
    }
}
