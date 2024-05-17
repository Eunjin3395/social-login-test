package com.example;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

// kakao 로그인을 위한 class
@Component
@Getter
@Slf4j
public class KakaoApi {

    @Value("${social.kakao.apikey}")
    private String kakaoApiKey;

    @Value("${social.kakao.redirect_uri}")
    private String kakaoRedirectUri;

    // 인증 code를 가지고 카카오 API 서버로부터 access token을 받아오는 메소드
    public String getAccessToken(String code) {
        String accessToken = "";
        String requestUrl = "https://kauth.kakao.com/oauth/token";

        try {
            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //필수 헤더 세팅
            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            // 요청 파라미터 설정
            Map<String, String> parameters = new HashMap<>();
            parameters.put("grant_type", "authorization_code");
            parameters.put("client_id", kakaoApiKey);
            parameters.put("redirect_uri", kakaoRedirectUri);
            parameters.put("code", code);

            // 요청 본문 작성
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, String> param : parameters.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(param.getKey());
                postData.append('=');
                postData.append(java.net.URLEncoder.encode(param.getValue(), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes(StandardCharsets.UTF_8);

            // 요청 본문 전송
            try (OutputStream os = conn.getOutputStream()) {
                os.write(postDataBytes);
                os.flush();
            }

            // 응답 코드 확인
            int responseCode = conn.getResponseCode();
            log.info("[KakaoApi.getAccessToken] responseCode = {}", responseCode);

            // 응답 본문 읽기
            StringBuilder response = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
            }

            // JSON 응답 파싱
            JSONObject jsonResponse = new JSONObject(response.toString());
            accessToken = jsonResponse.getString("access_token");
            String refreshToken = jsonResponse.getString("refresh_token");

            log.info("response JSON: {}", response.toString());
            log.info("access token: {}", accessToken);
            log.info("refresh token: {}", refreshToken);

            // 연결 종료
            conn.disconnect();


        } catch (Exception e) {
            e.printStackTrace();
        }

        return accessToken;
    }


    public Map<String, Object> getUserInfo(String accessToken) {

        HashMap<String, Object> userInfo = new HashMap<>();
        String requestUrl = "https://kapi.kakao.com/v2/user/me";

        try{
            // URL 설정
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();


            // 요청 메소드 및 헤더 설정
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            // 응답 코드 확인
            int responseCode = connection.getResponseCode();
            log.info("[KakaoApi.getUserInfo] responseCode = {}", responseCode);

            // 응답 본문 읽기
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            // JSON 응답 파싱
            JSONObject jsonResponse = new JSONObject(response.toString());
            long id = jsonResponse.getLong("id");
            String nickname = jsonResponse.getJSONObject("kakao_account").getJSONObject("profile").getString("nickname");
            String email = jsonResponse.getJSONObject("kakao_account").getString("email");

            log.info("response JSON: {}", response.toString());
            log.info("kakao user id: {}", id);
            log.info("kakao user nickname: {}", nickname);
            log.info("kakao user email: {}", email);


            // userInfo Map에 저장
            userInfo.put("nickname", nickname);
            userInfo.put("email", email);

            // 연결 종료
            connection.disconnect();

        } catch (Exception e){
            e.printStackTrace();
        }
        return userInfo;
    }
}
