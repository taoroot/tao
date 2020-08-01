package com.github.taoroot.tao.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * 检测到 AccessDeniedException, 如果是匿名用户, 将启动 authenticationEntryPoint
 */
public class CustomAuthenticationEntryPoint implements org.springframework.security.web.AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        if (!response.isCommitted()) {
            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            HashMap<String, String> message = new HashMap<>();
            message.put("message", "请先登录");
            message.put("documentation_url", "/swagger-ui.html");

            response.getWriter().write(new ObjectMapper().writeValueAsString(message));
        }
    }
}
