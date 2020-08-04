package com.github.taoroot.tao.security.captcha;

import com.github.taoroot.tao.security.captcha.support.ImageValidationFilter;
import com.github.taoroot.tao.security.captcha.support.SmsCodeValidationFilter;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CaptchaValidationConfigurer<H extends HttpSecurityBuilder<H>>
        extends AbstractHttpConfigurer<CaptchaValidationConfigurer<H>, H> {

    private Set<String> smsValidationUrls = new HashSet<>();

    private Set<String> imageValidationUrls = new HashSet<>();

    private CaptchaValidationRepository captchaValidationRepository;

    public CaptchaValidationConfigurer<H> smsValidationUrls(String... url) {
        smsValidationUrls.addAll(Arrays.asList(url));
        return this;
    }

    public CaptchaValidationConfigurer<H> imageValidationUrls(String... url) {
        imageValidationUrls.addAll(Arrays.asList(url));
        return this;
    }

    public CaptchaValidationConfigurer<H> smsCodeValidationRepository(CaptchaValidationRepository captchaValidationRepository) {
        this.captchaValidationRepository = captchaValidationRepository;
        return this;
    }

    @Override
    public void configure(H http) throws Exception {
        super.configure(http);
        SmsCodeValidationFilter smsCodeValidationFilter = new SmsCodeValidationFilter();
        http.addFilterBefore(smsCodeValidationFilter, UsernamePasswordAuthenticationFilter.class);
        smsCodeValidationFilter.smsCodeRepository(captchaValidationRepository);

        ImageValidationFilter imageValidationFilter = new ImageValidationFilter();
        smsCodeValidationFilter.smsCodeRepository(captchaValidationRepository);
        http.addFilterBefore(imageValidationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
