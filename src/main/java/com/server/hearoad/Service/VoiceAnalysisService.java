package com.server.hearoad.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.hearoad.Model.VoiceAnalysisResult;
import com.server.hearoad.Repository.VoiceAnalysisResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import javax.sound.sampled.*;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VoiceAnalysisService {

    private final WebClient.Builder webClientBuilder;
    private final VoiceAnalysisResultRepository voiceAnalysisResultRepository;
    private final ObjectMapper objectMapper;

    public VoiceAnalysisResult analyzeVoice(MultipartFile file, String nickname) throws Exception {
        Path tempMp3File = Files.createTempFile("voice_", "." + getFileExtension(file.getOriginalFilename()));
        file.transferTo(tempMp3File.toFile());

        Path tempWavFile = Files.createTempFile("voice_", ".wav");
        convertMp3ToWav(tempMp3File, tempWavFile);

        String fastApiResponse = sendFileToFastApi(tempWavFile);

        String character = extractCharacterFromJson(fastApiResponse);

        VoiceAnalysisResult result = new VoiceAnalysisResult(nickname, fastApiResponse, character);
        voiceAnalysisResultRepository.save(result);

        Files.delete(tempMp3File);
        Files.delete(tempWavFile);

        return result;
    }

    private void convertMp3ToWav(Path mp3FilePath, Path wavFilePath) throws Exception {
        try (InputStream mp3Stream = Files.newInputStream(mp3FilePath)) {
            AudioInputStream mp3AudioStream = AudioSystem.getAudioInputStream(mp3Stream);

            AudioFormat baseFormat = mp3AudioStream.getFormat();
            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false
            );

            try (AudioInputStream wavAudioStream = AudioSystem.getAudioInputStream(decodedFormat, mp3AudioStream)) {
                AudioSystem.write(wavAudioStream, AudioFileFormat.Type.WAVE, wavFilePath.toFile());
            }
        }
    }

    private String sendFileToFastApi(Path filePath) {
        WebClient webClient = webClientBuilder.build();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(filePath.toFile()));

        return webClient.post()
                .uri("http://localhost:8000/analyze-voice/")  // FastAPI 서버 주소
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    private String extractCharacterFromJson(String json) throws Exception {
        Map<String, Object> responseMap = objectMapper.readValue(json, Map.class);
        return responseMap.get("character").toString();
    }
}
