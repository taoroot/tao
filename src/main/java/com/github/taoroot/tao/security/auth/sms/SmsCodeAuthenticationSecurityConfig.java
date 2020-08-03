package com.github.taoroot.tao.security.auth.sms;

import com.github.taoroot.tao.security.CustomAuthenticationSuccessHandler;
import com.github.taoroot.tao.security.CustomUserDetailsService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class SmsCodeAuthenticationSecurityConfig<H extends HttpSecurityBuilder<H>>
        extends AbstractHttpConfigurer<SmsCodeAuthenticationSecurityConfig<H>, H> {

    private CustomUserDetailsService userDetailsService;

    private CustomAuthenticationSuccessHandler authenticationSuccessHandler;

    @Override
    public void configure(H http) throws Exception {
        super.configure(http);

        SmsCodeAuthenticationFilter smsCodeAuthenticationFilter = new SmsCodeAuthenticationFilter();
        smsCodeAuthenticationFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
        smsCodeAuthenticationFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler);

        SmsCodeAuthenticationProvider smsCodeAuthenticationProvider = new SmsCodeAuthenticationProvider();
        smsCodeAuthenticationProvider.setUserDetailService(userDetailsService);

        http.authenticationProvider(smsCodeAuthenticationProvider)
                .addFilterAfter(smsCodeAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    public SmsCodeAuthenticationSecurityConfig<H> userDetailsService(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
        return this;
    }

    public SmsCodeAuthenticationSecurityConfig<H> authenticationSuccessHandler(CustomAuthenticationSuccessHandler authenticationSuccessHandler) {
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        return this;
    }
}
