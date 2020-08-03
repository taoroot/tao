package com.github.taoroot.tao.security.auth.sms;

import com.github.taoroot.tao.security.CustomAuthenticationSuccessHandler;
import com.github.taoroot.tao.security.CustomUserDetailsService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SmsCodeAuthenticationSecurityConfig<H extends HttpSecurityBuilder<H>>
        extends AbstractHttpConfigurer<SmsCodeAuthenticationSecurityConfig<H>, H> {

    public static final String LOGIN_PATH = "/login/phone";

    private Set<String> validationUrls = new HashSet<>();

    private CustomUserDetailsService userDetailsService;

    private CustomAuthenticationSuccessHandler authenticationSuccessHandler;

    @Override
    public void configure(H http) throws Exception {
        super.configure(http);

        // 手机号登录
        SmsCodeAuthenticationFilter smsCodeAuthenticationFilter = new SmsCodeAuthenticationFilter(LOGIN_PATH);
        smsCodeAuthenticationFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
        smsCodeAuthenticationFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler);

        SmsCodeAuthenticationProvider smsCodeAuthenticationProvider = new SmsCodeAuthenticationProvider();
        smsCodeAuthenticationProvider.setUserDetailService(userDetailsService);
        http.authenticationProvider(smsCodeAuthenticationProvider)
                .addFilterBefore(smsCodeAuthenticationFilter, AnonymousAuthenticationFilter.class);


        // 手机验证码校验
        SmsCodeValidationFilter smsCodeValidationFilter = new SmsCodeValidationFilter();
        smsCodeValidationFilter.addUrl(LOGIN_PATH);
        validationUrls.forEach(smsCodeValidationFilter::addUrl);
        smsCodeValidationFilter.afterPropertiesSet();

        http.addFilterBefore(smsCodeValidationFilter, SmsCodeAuthenticationFilter.class);
    }

    public SmsCodeAuthenticationSecurityConfig<H> userDetailsService(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
        return this;
    }

    public SmsCodeAuthenticationSecurityConfig<H> authenticationSuccessHandler(CustomAuthenticationSuccessHandler authenticationSuccessHandler) {
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        return this;
    }

    public SmsCodeAuthenticationSecurityConfig<H> validationUrls(String... validationUrls) {
        this.validationUrls = new HashSet<>(Arrays.asList(validationUrls));
        return this;
    }
}
