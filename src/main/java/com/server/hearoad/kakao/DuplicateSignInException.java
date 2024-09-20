package com.server.hearoad.kakao;

public class DuplicateSignInException extends RuntimeException {
    public DuplicateSignInException() {
        super("해당 사용자는 이미 존재합니다.");
    }

    public DuplicateSignInException(String message) {
        super(message);
    }
}
