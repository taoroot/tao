package com.github.taoroot.tao.security.auth.oauth2;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import lombok.SneakyThrows;
import net.minidev.json.JSONObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登录成功,返回 Token
 */
public class CustomOAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final String secret;

    public CustomOAuth2AuthenticationSuccessHandler(String secret) {
        this.secret = secret;
    }

    @SneakyThrows
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (!response.isCommitted()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("sub", SecurityContextHolder.getContext().getAuthentication().getName());
            jsonObject.put("aud", "AUTH2");
            jsonObject.put("exp", System.currentTimeMillis() / 1000 + 24 * 60 * 60);

            JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256), new Payload(jsonObject));
            jwsObject.sign(new MACSigner(secret));

            String referer = (String) request.getSession().getAttribute("Referer");

            if (referer.indexOf("?") > 0) {
                response.sendRedirect(referer + "&token=" + jwsObject.serialize());
            } else {
                response.sendRedirect(referer + "?token=" + jwsObject.serialize());
            }
        }
    }

}
