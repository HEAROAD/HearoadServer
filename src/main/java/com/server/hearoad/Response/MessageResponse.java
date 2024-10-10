package com.server.hearoad.Response;

public class MessageResponse {
    private String originalMessage; // 원본 메시지
    private String keywords; // 추출된 핵심 단어

    public MessageResponse(String originalMessage, String keywords) {
        this.originalMessage = originalMessage;
        this.keywords = keywords;
    }

    // Getters and Setters
    public String getOriginalMessage() {
        return originalMessage;
    }

    public void setOriginalMessage(String originalMessage) {
        this.originalMessage = originalMessage;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
}
