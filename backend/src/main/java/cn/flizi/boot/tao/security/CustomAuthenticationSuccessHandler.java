package cn.flizi.boot.tao.security;

import cn.hutool.core.collection.CollUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.flizi.boot.tao.utils.R;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import lombok.SneakyThrows;
import net.minidev.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 登录成功,返回 Token
 */
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final String secret;

    public CustomAuthenticationSuccessHandler(String secret) {
        this.secret = secret;
    }

    @SneakyThrows
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (!response.isCommitted()) {
            CustomUserDetails principal = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("sub", "" +principal.getId());
            jsonObject.put("aud", "PASS");
            jsonObject.put("exp", System.currentTimeMillis() / 1000 + 24 * 60 * 60);
            jsonObject.put("scp", CollUtil.join(principal.getAuthorities(), " "));

            JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256), new Payload(jsonObject));
            jwsObject.sign(new MACSigner(secret));

            R<String> r = R.ok(jwsObject.serialize());

            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(new ObjectMapper().writeValueAsString(r));
        }
    }

}
