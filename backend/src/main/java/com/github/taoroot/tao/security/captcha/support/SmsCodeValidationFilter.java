package com.github.taoroot.tao.security.captcha.support;

import com.github.taoroot.tao.security.auth.sms.SmsCodeAuthenticationToken;
import com.github.taoroot.tao.security.captcha.CaptchaValidationException;
import com.github.taoroot.tao.security.captcha.CaptchaValidationRepository;
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
    public void afterPropertiesSet() throws ServletException {
        super.afterPropertiesSet();
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
        // 短信验证码
        String smsCode = obtainSmsCode(request);
        // 手机号
        String phone = obtainPhone(request);
        // 从缓存中获取Code
        String cacheCode = captchaValidationRepository.getCode(phone);

        if (smsCode == null || smsCode.isEmpty()) {
            throw new CaptchaValidationException("短信验证码不能为空");
        }

        if (cacheCode == null) {
            throw new CaptchaValidationException("验证码已失效");
        }

        if (!smsCode.toLowerCase().equals(cacheCode)) {
            throw new CaptchaValidationException("短信验证码错误");
        }
    }

    /**
     * 获取验证码
     */
    private String obtainSmsCode(HttpServletRequest request) {
        return request.getParameter("smsCode");
    }

    /**
     * 获取手机号
     */
    private String obtainPhone(HttpServletRequest request) {
        return request.getParameter(SmsCodeAuthenticationToken.PHONE);
    }
}
