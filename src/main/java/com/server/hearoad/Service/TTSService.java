package com.server.hearoad.Service;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechResponse;
import software.amazon.awssdk.services.polly.model.OutputFormat;
import software.amazon.awssdk.services.polly.model.VoiceId;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

@Service
public class TTSService {

    private static final Logger logger = LoggerFactory.getLogger(TTSService.class);
    private final PollyClient polly;

    public TTSService() {
        this.polly = PollyClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();
    }

    public String synthesizeSpeechToFile(String text, String outputDir) {
        String outputFileName = outputDir + "/" + UUID.randomUUID() + ".mp3";

        try {
            logger.info("TTS 요청: " + text);

            SynthesizeSpeechRequest synthReq = SynthesizeSpeechRequest.builder()
                    .text(text)
                    .voiceId(VoiceId.JOANNA)
                    .outputFormat(OutputFormat.MP3)
                    .build();

            SynthesizeSpeechResponse synthRes = polly.synthesizeSpeech(synthReq);

            try (InputStream in = synthRes.audioStream();
                 FileOutputStream out = new FileOutputStream(outputFileName)) {

                byte[] buffer = new byte[2 * 1024];
                int readBytes;

                while ((readBytes = in.read(buffer)) > 0) {
                    out.write(buffer, 0, readBytes);
                }

                logger.info("MP3 파일 생성: " + outputFileName);
                return outputFileName;

            } catch (Exception e) {
                logger.error("파일 저장 실패", e);
                return null;
            }

        } catch (Exception e) {
            logger.error("TTS 실패", e);
            return null;
        }
    }
}