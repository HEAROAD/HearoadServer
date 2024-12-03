package com.server.hearoad.Response;

public class MessageResponse {
    private String originalMessage;
    private String keywords;

    public MessageResponse(String originalMessage, String keywords) {
        this.originalMessage = originalMessage;
        this.keywords = keywords;
    }

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
