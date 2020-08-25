package com.github.taoroot.tao.security.auth.oauth2;

import cn.hutool.core.collection.CollUtil;
import com.github.taoroot.tao.security.CustomJwtDecoder;
import com.github.taoroot.tao.security.CustomUserDetails;
import com.github.taoroot.tao.security.CustomUserDetailsService;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import net.minidev.json.JSONObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * 登录成功,返回 Token
 */
@Log4j2
@Component
public class CustomOAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Resource
    private CustomJwtDecoder jwtDecoder;

    @Resource
    private CustomUserDetailsService userDetailsService;

    @SneakyThrows
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("用户登录成功 {}", authentication);

        if (response.isCommitted()) {
            return;
        }

        OAuth2AuthenticationToken oauth2Authentication = (OAuth2AuthenticationToken) authentication;
        CustomOAuth2User principal = (CustomOAuth2User) oauth2Authentication.getPrincipal();

        String redirectUrl = (String) request.getSession().getAttribute(OAuth2ParameterNames.REDIRECT_URI);
        redirectUrl += redirectUrl.contains("?") ? "&" : "?";

        String accessToken = (String) request.getSession().getAttribute(OAuth2ParameterNames.ACCESS_TOKEN);
        String clientId = oauth2Authentication.getAuthorizedClientRegistrationId();

        // 登录账号
        if (StringUtils.isEmpty(accessToken)) {
            // 如果没有,就创建用户
            JSONObject jsonObject = new JSONObject();
            CustomUserDetails customUserDetails = userDetailsService.loadUserByOAuth2(clientId, principal, true);
            jsonObject.put("sub", "" + customUserDetails.getId());
            jsonObject.put("aud", "auth2-" + oauth2Authentication.getAuthorizedClientRegistrationId());
            jsonObject.put("exp", System.currentTimeMillis() / 1000 + 24 * 60 * 60);
            jsonObject.put("scp", CollUtil.join(customUserDetails.getAuthorities(), " "));

            JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256), new Payload(jsonObject));
            jwsObject.sign(new MACSigner(jwtDecoder.getSecret()));
            response.sendRedirect(redirectUrl + OAuth2ParameterNames.ACCESS_TOKEN + "=" + jwsObject.serialize());
        }
        // 绑定社交账号
        else {
            CustomUserDetails customUserDetails = userDetailsService.loadUserByOAuth2(clientId, principal, false);

            // 已被绑定
            if (customUserDetails != null) {
                response.sendRedirect(redirectUrl + "msg=" + URLEncoder.encode("请先与 " + customUserDetails.getUsername() + " 解绑", "UTF-8"));
                return;
            }

            // 绑定
            Jwt decode = jwtDecoder.decode(accessToken);
            String username = decode.getSubject();
            String type = oauth2Authentication.getAuthorizedClientRegistrationId();
            String msg = userDetailsService.bindOauth2(clientId, principal, Integer.parseInt(username));
            log.info("用户: {} 绑定: {} : {}", username, type, principal.getName());
            response.sendRedirect(redirectUrl + "msg=" + URLEncoder.encode(msg, "UTF-8"));
        }
    }
}
