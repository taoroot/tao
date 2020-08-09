package com.github.taoroot.tao.security;

import com.github.taoroot.tao.security.auth.sms.SmsCodeAuthenticationConfigurer;
import com.github.taoroot.tao.security.captcha.CaptchaValidationConfigurer;
import com.github.taoroot.tao.security.captcha.support.InMemoryValidationRepository;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;

import javax.annotation.Resource;
import java.util.Collections;

@Component
public class CustomSecurityConfigurer extends WebSecurityConfigurerAdapter {

    public static final String secret = "secretsecretsecretsecretsecretsecret";
    public static final String FORM_LOGIN_PATH_KEY = "/login";

    @Resource
    private CustomUserDetailsService userDetailsService;

    // @formatter:off
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CustomAuthenticationEntryPoint customAuthenticationEntryPoint = new CustomAuthenticationEntryPoint();
        CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler = new CustomAuthenticationSuccessHandler(secret);

        // Basic 登录
        http
                .apply(new CaptchaValidationConfigurer<HttpSecurity>() // 验证码校验
                        .failureHandler(customAuthenticationEntryPoint::commence)
                        .captchaValidationRepository(new InMemoryValidationRepository())  // 验证码存入内存
//                        .smsValidationUrls(SmsCodeAuthenticationFilter.LOGIN_PATH_KEY)
                        .imageValidationUrls(FORM_LOGIN_PATH_KEY)).and() // 账号密码登录需要有图像验证码 // 手机号登录需要有手机号验证码
                .httpBasic(Customizer.withDefaults()) // BASIC 登录
                .formLogin(config -> { // 表单登录
                    config.loginProcessingUrl(FORM_LOGIN_PATH_KEY);
                    config.failureHandler(customAuthenticationEntryPoint::commence);
                    config.successHandler(customAuthenticationSuccessHandler);
                })
                .oauth2ResourceServer(config -> { // JWT登录
                    config.authenticationEntryPoint(customAuthenticationEntryPoint);
                    config.jwt().decoder(new CustomJwtDecoder(secret));
                })
                .apply(new SmsCodeAuthenticationConfigurer<HttpSecurity>() // 手机号登录
                        .userDetailsService(userDetailsService)
                        .failureHandler(customAuthenticationEntryPoint::commence)
                        .successHandler(customAuthenticationSuccessHandler)).and()
                .logout(config -> { // 退出登录
                    config.logoutSuccessHandler(new CustomLogoutSuccessHandler());
                })
                .cors(config -> config.configurationSource(req -> { // 跨域
                    CorsConfiguration corsConfiguration = new CorsConfiguration();
                    corsConfiguration.setAllowedOrigins(Collections.singletonList("*"));
                    corsConfiguration.setAllowedHeaders(Collections.singletonList("*"));
                    corsConfiguration.setAllowedMethods(Collections.singletonList("*"));
                    return corsConfiguration;
                }))
                .exceptionHandling(config -> { // 异常处理
                    config.authenticationEntryPoint(customAuthenticationEntryPoint);
                    config.accessDeniedHandler(new CustomAccessDeniedHandler());
                })
                .csrf().disable() // 禁用CSRF
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and() // 禁用 SESSION
                .authorizeRequests().anyRequest().authenticated(); // 所有请求
    }
    // @formatter:on
}
