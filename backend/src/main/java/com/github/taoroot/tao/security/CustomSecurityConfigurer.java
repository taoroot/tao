package com.github.taoroot.tao.security;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.stereotype.Component;

@Component
public class CustomSecurityConfigurer extends WebSecurityConfigurerAdapter {

    public static final String secret = "secretsecretsecretsecretsecretsecret";

    // @formatter:off
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CustomAuthenticationEntryPoint customAuthenticationEntryPoint = new CustomAuthenticationEntryPoint();
        CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler = new CustomAuthenticationSuccessHandler(secret);

//
        // Basic 登录
        http
                .csrf().disable()
                .httpBasic(Customizer.withDefaults())
                .formLogin(config -> {
                    config.failureHandler(customAuthenticationEntryPoint::commence);
                    config.successHandler(customAuthenticationSuccessHandler);
                })
                .exceptionHandling(config -> {
                    config.authenticationEntryPoint(customAuthenticationEntryPoint);
                    config.accessDeniedHandler(new CustomAccessDeniedHandler());
                })
                .authorizeRequests().anyRequest().authenticated();

//        // 密码登录
//        http.formLogin()
//                .failureHandler(customAuthenticationEntryPoint::commence)
//                .successHandler(customAuthenticationSuccessHandler);

        // JWT登录
//        http.oauth2ResourceServer()
//                .authenticationEntryPoint(customAuthenticationEntryPoint)
//                .jwt()
//                .decoder(new CustomJwtDecoder(secret));

        // 手机号登录
//        http.apply(new SmsCodeAuthenticationConfigurer<>())
//                .userDetailsService(userDetailsService)
//                .authenticationFailureHandler(customAuthenticationEntryPoint::commence)
//                .authenticationSuccessHandler(customAuthenticationSuccessHandler);

        // 社会登录
//        http.oauth2Login()
//                .successHandler(new CustomOAuth2AuthenticationSuccessHandler(secret))
//                .authorizationEndpoint()
//                .authorizationRequestResolver(new CustomOAuth2AuthorizationRequestResolver(
//                        http.getSharedObject(ApplicationContext.class).getBean(ClientRegistrationRepository.class),
//                        OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI))
//                .authorizationRequestRepository(new CustomHttpSessionOAuth2AuthorizationRequestRepository());

        // 验证码校验
//        http.apply(new CaptchaValidationConfigurer<>())
//                .captchaValidationRepository(new InMemoryValidationRepository()); // 验证码存入内存
//                .imageValidationUrls(CustomUsernamePasswordAuthenticationFilter.LOGIN_PATH_KEY) // 账号密码登录需要用有图像图像验证码
//                .smsValidationUrls(SmsCodeAuthenticationFilter.LOGIN_PATH_KEY); // 手机号登录需要有手机号验证码

        // 跨域
//        http.cors().configurationSource(req -> {
//            CorsConfiguration corsConfiguration = new CorsConfiguration();
//            corsConfiguration.setAllowedOrigins(Collections.singletonList("*"));
//            corsConfiguration.setAllowedHeaders(Collections.singletonList("*"));
//            corsConfiguration.setAllowedMethods(Collections.singletonList("*"));
//            return corsConfiguration;
//        });

        // 退出登录
//        http.logout()
//                .logoutSuccessHandler(new CustomLogoutSuccessHandler());

        // 系统自带配置
//        http
//                .csrf().disable()      // 禁用CSRF
//                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 禁用 SESSION
//                .and()
//                // 检测到 AccessDeniedException, 根据用户身份执行不同处理
//                .exceptionHandling()
//                // 如果是匿名用户, 将启动authenticationEntryPoint
//                .authenticationEntryPoint(customAuthenticationEntryPoint)
//                // 如果不是匿名用户，将启动AccessDeniedHandler
//                .accessDeniedHandler(new CustomAccessDeniedHandler())
//                .and()
//                .authorizeRequests().anyRequest().authenticated(); // 其他请求必须鉴权后访问
    }
    // @formatter:on

//    @Bean
//    CaptchaValidationRepository captchaValidationRepository() {
//        return new InMemoryValidationRepository();
//    }
}
