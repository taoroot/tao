package com.github.taoroot.tao.security;

import com.github.taoroot.tao.security.auth.oauth2.CustomHttpSessionOAuth2AuthorizationRequestRepository;
import com.github.taoroot.tao.security.auth.oauth2.CustomOAuth2AuthenticationSuccessHandler;
import com.github.taoroot.tao.security.auth.oauth2.CustomOAuth2AuthorizationRequestResolver;
import com.github.taoroot.tao.security.auth.password.CustomUsernamePasswordSecurityConfigurer;
import com.github.taoroot.tao.security.auth.sms.SmsCodeAuthenticationConfigurer;
import com.github.taoroot.tao.security.captcha.CaptchaValidationRepository;
import com.github.taoroot.tao.security.captcha.support.InMemoryValidationRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.web.cors.CorsConfiguration;

import javax.annotation.Resource;
import java.util.Collections;

@EnableWebSecurity
public class CustomSecurityConfigurer extends WebSecurityConfigurerAdapter {

    public static final String secret = "secretsecretsecretsecretsecretsecret";

    @Resource
    private CustomUserDetailsService userDetailsService;

    @Resource
    private CaptchaValidationRepository captchaValidationRepository;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        super.configure(auth);
    }

    // @formatter:off
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CustomAuthenticationEntryPoint customAuthenticationEntryPoint = new CustomAuthenticationEntryPoint();
        CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler = new CustomAuthenticationSuccessHandler(secret);

        // 自定义密码登录器
        http.apply(new CustomUsernamePasswordSecurityConfigurer<>())
                .authenticationManager(authenticationManagerBean())
                .userDetailsService(userDetailsService)
                .successHandler(customAuthenticationSuccessHandler);

        // 自定义JWT登录器
        http.oauth2ResourceServer()
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                .jwt()
                .decoder(new CustomJwtDecoder(secret));

        // 手机号登录
        http.apply(new SmsCodeAuthenticationConfigurer<>())
                .userDetailsService(userDetailsService)
                .authenticationSuccessHandler(customAuthenticationSuccessHandler);

        // 社会登录
        http.oauth2Login()
                .successHandler(new CustomOAuth2AuthenticationSuccessHandler(secret))
                .authorizationEndpoint()
                .authorizationRequestResolver(new CustomOAuth2AuthorizationRequestResolver(
                        http.getSharedObject(ApplicationContext.class).getBean(ClientRegistrationRepository.class),
                        OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI))
                .authorizationRequestRepository(new CustomHttpSessionOAuth2AuthorizationRequestRepository());

        // 验证码校验
//        http.apply(new CaptchaValidationConfigurer<>())
//                .captchaValidationRepository(new InMemoryValidationRepository()); // 验证码存入内存
//                .imageValidationUrls(CustomUsernamePasswordAuthenticationFilter.LOGIN_PATH_KEY) // 账号密码登录需要用有图像图像验证码
//                .smsValidationUrls(SmsCodeAuthenticationFilter.LOGIN_PATH_KEY); // 手机号登录需要有手机号验证码

        // 跨域
        http.cors().configurationSource(req -> {
            CorsConfiguration corsConfiguration = new CorsConfiguration();
            corsConfiguration.setAllowedOrigins(Collections.singletonList("*"));
            corsConfiguration.setAllowedHeaders(Collections.singletonList("*"));
            corsConfiguration.setAllowedMethods(Collections.singletonList("*"));
            return corsConfiguration;
        });

        // 系统自带配置
        http
                .csrf().disable()      // 禁用CSRF
                .formLogin().disable() // 禁用表单登录
                .httpBasic().disable() // 禁用Basic登录
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 禁用 SESSION

                .and()
                // 检测到 AccessDeniedException, 根据用户身份执行不同处理
                .exceptionHandling()
                // 如果是匿名用户, 将启动authenticationEntryPoint
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                // 如果不是匿名用户，将启动AccessDeniedHandler
                .accessDeniedHandler(new CustomAccessDeniedHandler())

                .and()
                .authorizeRequests().anyRequest().authenticated(); // 其他请求必须鉴权后访问
    }
    // @formatter:on


    @Bean
    CaptchaValidationRepository captchaValidationRepository() {
        return new InMemoryValidationRepository();
    }
}
