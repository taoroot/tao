package com.github.taoroot.tao.security.captcha;

import com.github.taoroot.tao.security.captcha.support.ImageValidationFilter;
import com.github.taoroot.tao.security.captcha.support.SmsCodeValidationFilter;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CaptchaValidationConfigurer<H extends HttpSecurityBuilder<H>>
        extends AbstractHttpConfigurer<CaptchaValidationConfigurer<H>, H> {

    private final Set<String> smsValidationUrls = new HashSet<>();

    private final Set<String> imageValidationUrls = new HashSet<>();

    private CaptchaValidationRepository captchaValidationRepository;

    private AuthenticationFailureHandler authenticationFailureHandler;

    public CaptchaValidationConfigurer<H> captchaValidationRepository(CaptchaValidationRepository captchaValidationRepository) {
        this.captchaValidationRepository = captchaValidationRepository;
        return this;
    }

    public CaptchaValidationConfigurer<H> smsValidationUrls(String... url) {
        smsValidationUrls.addAll(Arrays.asList(url));
        return this;
    }

    public CaptchaValidationConfigurer<H> imageValidationUrls(String... url) {
        imageValidationUrls.addAll(Arrays.asList(url));
        return this;
    }

    public CaptchaValidationConfigurer<H> failureHandler(AuthenticationFailureHandler authenticationFailureHandler) {
        this.authenticationFailureHandler = authenticationFailureHandler;
        return this;
    }

    @Override
    public void configure(H http) throws Exception {
        super.configure(http);
        SmsCodeValidationFilter smsCodeValidationFilter = new SmsCodeValidationFilter();
        smsCodeValidationFilter.captchaValidationRepository(captchaValidationRepository);
        smsCodeValidationFilter.authenticationFailureHandler(authenticationFailureHandler);
        smsValidationUrls.forEach(smsCodeValidationFilter::addUrl);
        http.addFilterBefore(smsCodeValidationFilter, UsernamePasswordAuthenticationFilter.class);

        ImageValidationFilter imageValidationFilter = new ImageValidationFilter();
        imageValidationFilter.captchaValidationRepository(captchaValidationRepository);
        imageValidationFilter.authenticationFailureHandler(authenticationFailureHandler);
        imageValidationUrls.forEach(imageValidationFilter::addUrl);
        http.addFilterBefore(imageValidationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
