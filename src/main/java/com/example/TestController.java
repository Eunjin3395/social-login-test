package com.example;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final KakaoApiService kakaoApiService;
    private final MemberService memberService;

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("kakaoApiServiceKey", kakaoApiService.getKakaoApiKey());
        model.addAttribute("redirectUri", kakaoApiService.getKakaoRedirectUri());
        return "login";
    }

    @RequestMapping("/login/oauth2/code/kakao")
    public String kakaoLogin(@RequestParam String code, RedirectAttributes redirectAttributes) { // 1. redirectUri의 쿼리파라미터로 넘어온 code를 받음
        // 2. 토큰 받기
        String accessToken = kakaoApiService.getAccessToken(code);

        // 3. 사용자 정보 받기
        Map<String, Object> userInfo = kakaoApiService.getUserInfo(accessToken);

        String email = (String) userInfo.get("email");
        String nickname = (String) userInfo.get("nickname");

        log.info("email: {}", email);
        log.info("nickname: {}", nickname);

        // 4. 사용자 정보를 가지고 우리 서비스에 로그인/회원가입시키기
        Member member = memberService.login(email, nickname);

        redirectAttributes.addFlashAttribute("email", member.getEmail());
        redirectAttributes.addFlashAttribute("name", member.getName());


        return "redirect:/main";
    }

    @GetMapping("/main")
    public String mainPage(Model model) {
        return "main";
    }
}
