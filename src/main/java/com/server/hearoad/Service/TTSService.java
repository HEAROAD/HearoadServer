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

import org.springframework.stereotype.Service;
@Service
public class TTSService {

    private final PollyClient polly;

    public TTSService() {
        // Polly 클라이언트 초기화
        this.polly = PollyClient.builder()
                .region(Region.US_EAST_1)  // AWS 리전 설정
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();
    }

    public String synthesizeSpeechToFile(String text, String outputFileName) {
        try {
            SynthesizeSpeechRequest synthReq = SynthesizeSpeechRequest.builder()
                    .text(text)
                    .voiceId(VoiceId.JOANNA)
                    .outputFormat(OutputFormat.MP3)
                    .build();

            // Polly에서 음성 생성
            SynthesizeSpeechResponse synthRes = polly.synthesizeSpeech(synthReq);

            // MP3 파일로 저장
            try (InputStream in = synthRes.audioStream();
                 FileOutputStream out = new FileOutputStream(outputFileName)) {

                byte[] buffer = new byte[2 * 1024];
                int readBytes;

                while ((readBytes = in.read(buffer)) > 0) {
                    out.write(buffer, 0, readBytes);
                }

                return outputFileName;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}