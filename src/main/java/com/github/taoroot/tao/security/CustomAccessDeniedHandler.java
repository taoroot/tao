
package com.github.taoroot.tao.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 *  检测到 AccessDeniedException, 如果不是匿名用户，将启动AccessDeniedHandler
 */
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        if (!response.isCommitted()) {

            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            HashMap<String, String> message = new HashMap<>();
            message.put("message", "权限不足: " + request.getRequestURI());
            message.put("documentation_url", "/swagger-ui.html");

            response.getWriter().write(new ObjectMapper().writeValueAsString(message));
        }
    }
}
