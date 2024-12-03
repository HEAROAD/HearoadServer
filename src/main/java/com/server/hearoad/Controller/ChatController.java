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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final MessageService messageService;
    private final KakaoService kakaoService;

    private final String fastApiServerUrl = "http://localhost:8082";

    @PostMapping("/room")
    public ResponseEntity<ChatRoom> createChatRoom(@RequestBody ChatRoom chatRoom, HttpServletRequest request) {
        String userId = kakaoService.getUserIdFromToken(request.getHeader("Authorization").substring(7));
        ChatRoom createdChatRoom = chatRoomService.createChatRoom(userId, chatRoom.getTitle());
        return ResponseEntity.ok(createdChatRoom);
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoom>> getChatRooms(HttpServletRequest request) {
        String userId = kakaoService.getUserIdFromToken(request.getHeader("Authorization").substring(7));
        return ResponseEntity.ok(chatRoomService.getChatRoomsByUserId(userId));
    }

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
        String processedMessage = null;
        String keywords = null;

        if ("USER".equals(type)) {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }

            String fileExtension = getFileExtension(file.getOriginalFilename());
            if (!isVideoFile(fileExtension)) {
                return ResponseEntity.status(415).body(null);
            }

            try {
                processedMessage = sendFileToFastApiServer(file);
                fileData = Base64.getEncoder().encodeToString(file.getBytes());
                fileName = file.getOriginalFilename();
            } catch (IOException e) {
                return ResponseEntity.status(500).body(null);
            }
        } else if ("PARTNER".equals(type)) {
            if (message == null || message.isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }

            keywords = extractKeywordsFromMessage(message);
            processedMessage = message;
        } else {
            return ResponseEntity.badRequest().body(null);
        }

        Message newMessage = messageService.sendMessage(
                chatRoomId,
                userId,
                type,
                processedMessage,
                fileUrl,
                fileData,
                fileName
        );

        MessageResponse response = new MessageResponse(newMessage.getMessage(), keywords);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/messages/{chatRoomId}")
    public ResponseEntity<List<Message>> getMessages(@PathVariable String chatRoomId) {
        return ResponseEntity.ok(messageService.getMessagesByChatRoomId(chatRoomId));
    }

    private String sendFileToFastApiServer(MultipartFile file) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file.getResource());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(fastApiServerUrl + "/predict", requestEntity, String.class);

            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "Prediction Failed";
        }
    }

    private String extractKeywordsFromMessage(String message) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = new HashMap<>();
            body.put("message", message);

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(fastApiServerUrl + "/extract_keywords", requestEntity, Map.class);

            Map<String, String> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("keywords")) {
                return responseBody.get("keywords");
            } else {
                return "Keyword Extraction Failed";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Keyword Extraction Failed";
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private boolean isVideoFile(String fileExtension) {
        return fileExtension.equals("mp4") || fileExtension.equals("avi") || fileExtension.equals("mov") || fileExtension.equals("mkv");
    }
}
