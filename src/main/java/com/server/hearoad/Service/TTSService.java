package com.server.hearoad.Service;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechResponse;
import software.amazon.awssdk.services.polly.model.OutputFormat;
import software.amazon.awssdk.services.polly.model.VoiceId;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.PutObjectAclRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TTSService {

    private static final Logger logger = LoggerFactory.getLogger(TTSService.class);
    private final PollyClient polly;
    private final S3Client s3Client;
    private final String bucketName = "hearoad";

    public TTSService() {
        this.polly = PollyClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();

        this.s3Client = S3Client.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();
    }

    public String synthesizeSpeechToFileAndUpload(String text, String emoji) {
        String outputFileName = UUID.randomUUID() + ".mp3";

        try {
            logger.info("TTS 요청: " + text + " " + emoji);

            SynthesizeSpeechRequest synthReq = SynthesizeSpeechRequest.builder()
                    .text(text)
                    .voiceId(VoiceId.JOANNA)
                    .outputFormat(OutputFormat.MP3)
                    .build();

            ResponseInputStream<SynthesizeSpeechResponse> synthRes = polly.synthesizeSpeech(synthReq);

            try (InputStream in = synthRes;
                 FileOutputStream out = new FileOutputStream(outputFileName)) {

                byte[] buffer = new byte[2 * 1024];
                int readBytes;

                while ((readBytes = in.read(buffer)) > 0) {
                    out.write(buffer, 0, readBytes);
                }

                logger.info("MP3 파일 생성: " + outputFileName);

                // 파일을 S3에 업로드
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(outputFileName)
                        .build();

                PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, Paths.get(outputFileName));

                logger.info("S3 업로드 완료: " + outputFileName);

                // 정적 URL 반환
                String publicUrl = "https://" + bucketName + ".s3.amazonaws.com/" + outputFileName;
                return publicUrl;

            } catch (Exception e) {
                logger.error("파일 저장 실패", e);
                return null;
            }

        } catch (S3Exception e) {
            logger.error("S3 업로드 실패", e);
            return null;
        }
    }
}
