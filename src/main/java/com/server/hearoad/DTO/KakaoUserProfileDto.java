package com.server.hearoad.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoUserProfileDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("properties")
    private Properties properties;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {
        @JsonProperty("nickname")
        private String nickname;
        @JsonProperty("profile_image")
        private String profileImage;
    }
}

