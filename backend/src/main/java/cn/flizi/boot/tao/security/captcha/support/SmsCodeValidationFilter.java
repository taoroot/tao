package cn.flizi.boot.tao.security.captcha.support;

import cn.flizi.boot.tao.security.auth.sms.SmsCodeAuthenticationToken;
import cn.flizi.boot.tao.security.captcha.CaptchaValidationException;
import cn.flizi.boot.tao.security.captcha.CaptchaValidationRepository;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SmsCodeValidationFilter extends OncePerRequestFilter {

    private final Set<String> urls = new HashSet<>();

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    private AuthenticationFailureHandler authenticationFailureHandler;

    private CaptchaValidationRepository captchaValidationRepository;

    public void authenticationFailureHandler(AuthenticationFailureHandler authenticationFailureHandler) {
        this.authenticationFailureHandler = authenticationFailureHandler;
    }

    public void captchaValidationRepository(CaptchaValidationRepository captchaValidationRepository) {
        this.captchaValidationRepository = captchaValidationRepository;
    }

    public void addUrl(String... url) {
        Collections.addAll(urls, url);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        boolean action = urls.stream()
                .anyMatch(url -> antPathMatcher.match(url, request.getRequestURI()));

        if (action) {
            try {
                validate(request);
            } catch (AuthenticationException e) {
                authenticationFailureHandler.onAuthenticationFailure(request, response, e);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void validate(HttpServletRequest request) {
        String key = obtainKey(request);
        String code = obtainCode(request);
        String cacheCode = captchaValidationRepository.getCode(key);

        if (key == null || key.isEmpty()) {
            throw new CaptchaValidationException("短信验证码不能为空");
        }

        if (cacheCode == null) {
            throw new CaptchaValidationException("短信验证码已失效");
        }

        if (!code.toLowerCase().equals(cacheCode)) {
            throw new CaptchaValidationException("短信验证码错误");
        }
    }

    /**
     * 获取验证码
     */
    private String obtainKey(HttpServletRequest request) {
        return request.getParameter(SmsCodeAuthenticationToken.PHONE);
    }

    /**
     * 获取手机号
     */
    private String obtainCode(HttpServletRequest request) {
        return request.getParameter("smsCode");
    }
}
