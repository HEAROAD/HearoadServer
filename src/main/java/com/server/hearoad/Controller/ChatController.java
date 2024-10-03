package com.server.hearoad.Controller;

import com.server.hearoad.Model.ChatRoom;
import com.server.hearoad.Model.Message;
import com.server.hearoad.Response.MessageResponse;
import com.server.hearoad.Service.ChatRoomService;
import com.server.hearoad.Service.KakaoService;
import com.server.hearoad.Service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final MessageService messageService;
    private final KakaoService kakaoService;

    // FastAPI 서버 URL 설정
    private final String fastApiServerUrl = "http://localhost:8000"; // FastAPI 서버 주소 설정

    // 채팅방 생성
    @PostMapping("/room")
    public ResponseEntity<ChatRoom> createChatRoom(@RequestBody ChatRoom chatRoom, HttpServletRequest request) {
        String userId = kakaoService.getUserIdFromToken(request.getHeader("Authorization").substring(7));
        ChatRoom createdChatRoom = chatRoomService.createChatRoom(userId, chatRoom.getTitle());
        return ResponseEntity.ok(createdChatRoom);
    }

    // 채팅방 목록 조회
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoom>> getChatRooms(HttpServletRequest request) {
        String userId = kakaoService.getUserIdFromToken(request.getHeader("Authorization").substring(7));
        return ResponseEntity.ok(chatRoomService.getChatRoomsByUserId(userId));
    }

    // 메시지 전송 (채팅방 ID를 헤더로 받음)
    @PostMapping("/message")
    public ResponseEntity<MessageResponse> sendMessage(
            @RequestPart(value = "message", required = false) String message,
            @RequestPart(value = "type") String type,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestHeader("chatRoomId") String chatRoomId,
            HttpServletRequest request) {

        String userId = kakaoService.getUserIdFromToken(request.getHeader("Authorization").substring(7));
        String fileUrl = null;
        String fileData = null;
        String fileName = null;
        String processedMessage = null; // FastAPI 서버에서 반환된 메시지를 저장할 변수
        String keywords = null; // 핵심 단어 저장할 변수

        // 조건에 따른 처리
        if ("USER".equals(type)) {
            // type이 USER일 때, 영상 파일(MP4 등) 확인
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(null); // 파일이 없을 경우 오류 처리
            }

            // 파일 형식 검증 (예: .mp4)
            String fileExtension = getFileExtension(file.getOriginalFilename());
            if (!isVideoFile(fileExtension)) {
                return ResponseEntity.status(415).body(null); // 지원하지 않는 미디어 타입 415 반환
            }

            try {
                // FastAPI 서버로 파일 전송 및 응답 처리
                processedMessage = sendFileToFastApiServer(file);

                // 파일을 Base64 인코딩하여 저장
                fileData = Base64.getEncoder().encodeToString(file.getBytes());
                fileName = file.getOriginalFilename();
            } catch (IOException e) {
                return ResponseEntity.status(500).body(null); // 파일 처리 오류
            }
        } else if ("PARTNER".equals(type)) {
            // type이 PARTNER일 때, message가 필요
            if (message == null || message.isEmpty()) {
                return ResponseEntity.badRequest().body(null); // 메시지가 없을 경우 오류 처리
            }

            // 핵심 단어 추출 로직 추가
            keywords = extractKeywordsFromMessage(message);
            processedMessage = message; // 원본 메시지를 processedMessage로 할당
        } else {
            return ResponseEntity.badRequest().body(null); // 지원하지 않는 type일 경우 오류 처리
        }

        // 원본 메시지와 추출된 키워드를 MessageResponse로 반환
        MessageResponse response = new MessageResponse(processedMessage, keywords);
        return ResponseEntity.ok(response);
    }

    // 특정 채팅방의 메시지 조회
    @GetMapping("/messages/{chatRoomId}")
    public ResponseEntity<List<Message>> getMessages(@PathVariable String chatRoomId) {
        return ResponseEntity.ok(messageService.getMessagesByChatRoomId(chatRoomId));
    }

    // FastAPI 서버로 파일을 전송하고 응답을 받는 메서드
    private String sendFileToFastApiServer(MultipartFile file) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // 파일을 MultiValueMap에 추가
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file.getResource());

            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // FastAPI 서버로 POST 요청 보내기
            ResponseEntity<String> response = restTemplate.postForEntity(fastApiServerUrl + "/predict", requestEntity, String.class);

            // FastAPI 서버의 예측 결과 반환
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "Prediction Failed"; // 예측 실패 시 기본 메시지 반환
        }
    }

    // FastAPI 서버로 메시지를 전송하고 핵심 단어를 추출하는 메서드
    private String extractKeywordsFromMessage(String message) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 요청 바디 설정
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("message", message);

            // 요청 엔티티 생성
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

            // FastAPI 서버로 POST 요청 보내기
            ResponseEntity<String> response = restTemplate.postForEntity(fastApiServerUrl + "/extract_keywords", requestEntity, String.class);

            // FastAPI 서버로부터 받은 키워드 반환
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "Keyword Extraction Failed"; // 오류 시 반환 메시지
        }
    }

    // 파일 확장자를 확인하는 메서드
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(); // 확장자를 소문자로 반환
    }

    // 파일이 영상 파일인지 확인하는 메서드
    private boolean isVideoFile(String fileExtension) {
        return fileExtension.equals("mp4") || fileExtension.equals("avi") || fileExtension.equals("mov") || fileExtension.equals("mkv");
    }
}
