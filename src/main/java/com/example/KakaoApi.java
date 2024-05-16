package com.example;


import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// kakao secret key를 위한 class
@Component
@Getter
public class KakaoApi {

    @Value("${social.kakao.apikey}")
    private String kakaoApiKey;

    @Value("${social.kakao.redirect_uri}")
    private String kakaoRedirectUri;
}
