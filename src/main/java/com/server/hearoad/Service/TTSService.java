package com.server.hearoad.Service;

import com.server.hearoad.DTO.TTSFile;
import com.server.hearoad.Model.User;
import com.server.hearoad.Repository.TTSFileRepository;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechResponse;
import software.amazon.awssdk.services.polly.model.OutputFormat;
import software.amazon.awssdk.services.polly.model.VoiceId;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

@Service
public class TTSService {

    private static final Logger logger = LoggerFactory.getLogger(TTSService.class);
    private final PollyClient polly;
    private final S3Client s3Client;
    private final TTSFileRepository ttsFileRepository;

    @Value("${aws.s3.bucket-name:hearoad}")
    private String bucketName;

    private static final List<String> DEFAULT_TTS_FILES = Arrays.asList(
            "https://hearoad.s3.amazonaws.com/355581f0-cc62-4ec3-b154-a13e29294513.mp3",
            "https://hearoad.s3.amazonaws.com/b9e2581e-dc58-471f-9c4c-fd823aff8186.mp3",
            "https://hearoad.s3.amazonaws.com/c976305a-307c-4744-b46e-674f843ab5ec.mp3"
    );

    public TTSService(TTSFileRepository ttsFileRepository) {
        this.polly = PollyClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();

        this.s3Client = S3Client.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();

        this.ttsFileRepository = ttsFileRepository;
    }

    public void generateDefaultFilesForUser(User user) {
        if (user == null) return;

        for (String url : DEFAULT_TTS_FILES) {
            TTSFile ttsFile = new TTSFile();
            ttsFile.setUser(user);
            ttsFile.setFilePath(url);
            ttsFileRepository.save(ttsFile);
        }
    }

    public String synthesizeSpeechToFileAndUpload(String text, String emoji) {
        String outputFileName = UUID.randomUUID().toString() + ".mp3";
        File outputFile = new File(outputFileName);

        try {
            logger.info("TTS 요청: {} {}", text, emoji);

            SynthesizeSpeechRequest synthReq = SynthesizeSpeechRequest.builder()
                    .text(text)
                    .voiceId(VoiceId.SEOYEON)
                    .outputFormat(OutputFormat.MP3)
                    .build();

            ResponseInputStream<SynthesizeSpeechResponse> synthRes = polly.synthesizeSpeech(synthReq);

            try (InputStream in = synthRes; FileOutputStream out = new FileOutputStream(outputFile)) {

                byte[] buffer = new byte[2 * 1024];
                int readBytes;

                while ((readBytes = in.read(buffer)) > 0) {
                    out.write(buffer, 0, readBytes);
                }

                logger.info("MP3 파일 생성: {}", outputFileName);

                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(outputFileName)
                        .build();

                s3Client.putObject(putObjectRequest, Paths.get(outputFileName));

                logger.info("S3 업로드 완료: {}", outputFileName);

                return "https://" + bucketName + ".s3.amazonaws.com/" + outputFileName;

            } catch (Exception e) {
                logger.error("파일 저장 실패", e);
                return null;
            } finally {
                if (outputFile.exists() && !outputFile.delete()) {
                    logger.warn("로컬 파일 삭제 실패: {}", outputFileName);
                }
            }

        } catch (S3Exception e) {
            logger.error("S3 업로드 실패", e);
            return null;
        }
    }

    public boolean deleteFileFromS3(String filePath) {
        try {
            String key = filePath.substring(filePath.lastIndexOf("/") + 1);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            logger.info("S3에서 파일 삭제: {}", filePath);
            return true;
        } catch (S3Exception e) {
            logger.error("S3에서 파일 삭제 실패: {}", filePath, e);
            return false;
        }
    }
}
