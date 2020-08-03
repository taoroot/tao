package com.github.taoroot.tao.security.auth.sms;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class SmsCodeValidateFilter extends OncePerRequestFilter {

    private final Set<String> urls = new HashSet<>();

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    private AuthenticationFailureHandler authenticationFailureHandler;

    private SmsCodeValidateRepository smsCodeValidateRepository;

    public void authenticationFailureHandler(AuthenticationFailureHandler authenticationFailureHandler) {
        this.authenticationFailureHandler = authenticationFailureHandler;
    }

    public void smsCodeRepository(SmsCodeValidateRepository smsCodeValidateRepository) {
        this.smsCodeValidateRepository = smsCodeValidateRepository;
    }

    public Set<String> addUrl(String url) {
        return urls;
    }

    @Override
    public void afterPropertiesSet() throws ServletException {
        super.afterPropertiesSet();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        boolean action = urls.stream()
                .anyMatch(url -> antPathMatcher.match(url, request.getRequestURI()));

        if (!action) {
            filterChain.doFilter(request, response);
        }

        try {
            validate(request);
        } catch (AuthenticationException e) {
            authenticationFailureHandler.onAuthenticationFailure(request, response, e);
        }
    }

    private void validate(HttpServletRequest request) {
        // 短信验证码
        String smsCode = obtainSmsCode(request);
        // 手机号
        String phone = obtainPhone(request);
        // 从缓存中获取Code
        String cacheCode = smsCodeValidateRepository.getCode(phone);

        if (smsCode == null || smsCode.isEmpty()) {
            throw new SmsCodeValidateException("短信验证码不能为空");
        }

        if (cacheCode == null) {
            throw new SmsCodeValidateException("验证码已失效");
        }

        if (!smsCode.toLowerCase().equals(cacheCode)) {
            throw new SmsCodeValidateException("短信验证码错误");
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
