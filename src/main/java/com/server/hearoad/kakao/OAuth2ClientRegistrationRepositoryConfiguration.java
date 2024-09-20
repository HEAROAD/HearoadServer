package com.server.hearoad.kakao;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.oauth2.client.ClientsConfiguredCondition;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(OAuth2ClientProperties.class)
@Conditional(ClientsConfiguredCondition.class)
@RequiredArgsConstructor
public class OAuth2ClientRegistrationRepositoryConfiguration {

    private final OAuth2ClientProperties properties;

    @Bean
    @ConditionalOnMissingBean(ClientsConfiguredCondition.class)
    public InMemoryClientRegistrationRepository clientRegistrationRepository() {
        List<ClientRegistration> registrations = new ArrayList<>();

        // properties를 이용해 클라이언트 등록을 만듦
        Map<String, OAuth2ClientProperties.Registration> registrationsMap = properties.getRegistration();
        registrationsMap.forEach((key, registration) -> {
            ClientRegistration clientRegistration = ClientRegistration.withRegistrationId(key)
                    .clientId(registration.getClientId())
                    .clientSecret(registration.getClientSecret())
                    .clientAuthenticationMethod(authenticationMethod(registration.getClientAuthenticationMethod()))
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri(registration.getRedirectUri())
                    .authorizationUri(properties.getProvider().get(key).getAuthorizationUri())
                    .tokenUri(properties.getProvider().get(key).getTokenUri())
                    .userInfoUri(properties.getProvider().get(key).getUserInfoUri())
                    .userNameAttributeName(properties.getProvider().get(key).getUserNameAttribute())
                    .build();

            registrations.add(clientRegistration);
        });

        return new InMemoryClientRegistrationRepository(registrations);
    }

    private ClientAuthenticationMethod authenticationMethod(String method) {
        if ("post".equalsIgnoreCase(method)) {
            return new ClientAuthenticationMethod("post");
        }
        return new ClientAuthenticationMethod("basic");
    }

}
