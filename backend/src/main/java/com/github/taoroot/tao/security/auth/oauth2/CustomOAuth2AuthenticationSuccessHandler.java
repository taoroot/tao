package com.github.taoroot.tao.security.auth.oauth2;

import com.github.taoroot.tao.security.CustomJwtDecoder;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import net.minidev.json.JSONObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登录成功,返回 Token
 */
@Log4j2
@Component
public class CustomOAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Resource
    private CustomJwtDecoder jwtDecoder;


    @SneakyThrows
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("用户登录成功 {}", authentication);

        if (response.isCommitted()) {
            return;
        }

        OAuth2AuthenticationToken authentication1 = (OAuth2AuthenticationToken) authentication;
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        String referer = (String) request.getSession().getAttribute("Referer");

        String accessToken = (String) request.getSession().getAttribute("access_token");
        // 注册新账号
        if (StringUtils.isEmpty(accessToken)) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("sub", name);
            jsonObject.put("aud", "auth2-" + authentication1.getAuthorizedClientRegistrationId());
            jsonObject.put("exp", System.currentTimeMillis() / 1000 + 24 * 60 * 60);

            JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256), new Payload(jsonObject));

            jwsObject.sign(new MACSigner(jwtDecoder.getSecret()));

            if (referer.indexOf("?") > 0) {
                response.sendRedirect(referer + "&token=" + jwsObject.serialize());
            } else {
                response.sendRedirect(referer + "?token=" + jwsObject.serialize());
            }
        }
        // 绑定已有账号
        else {
            Jwt decode = jwtDecoder.decode(accessToken);
            String username = decode.getSubject();
            String type = authentication1.getAuthorizedClientRegistrationId();
            // 成功
            log.info("用户: {} 绑定: {} : {}", username, type, name);
            String msg = "" + name;
            if (referer.indexOf("?") > 0) {
                response.sendRedirect(referer + "&msg=" + msg);
            } else {
                response.sendRedirect(referer + "?msg=" + msg);
            }
        }
    }


}
