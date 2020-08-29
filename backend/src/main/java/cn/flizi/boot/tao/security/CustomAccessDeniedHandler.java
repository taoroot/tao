
package cn.flizi.boot.tao.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import cn.flizi.boot.tao.utils.R;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 检测到 AccessDeniedException, 如果不是匿名用户，将启动AccessDeniedHandler
 */
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        if (!response.isCommitted()) {
            R<String> r = R.errMsg("权限不足: " + request.getRequestURI());

            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(new ObjectMapper().writeValueAsString(r));
        }
    }
}
