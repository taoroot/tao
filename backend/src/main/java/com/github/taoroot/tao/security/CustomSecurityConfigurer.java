package com.github.taoroot.tao.security;

import cn.hutool.core.util.ReUtil;
import com.github.taoroot.tao.security.annotation.NotAuth;
import com.github.taoroot.tao.security.auth.oauth2.*;
import com.github.taoroot.tao.security.auth.sms.SmsCodeAuthenticationConfigurer;
import com.github.taoroot.tao.security.auth.sms.SmsCodeAuthenticationFilter;
import com.github.taoroot.tao.security.captcha.CaptchaValidationConfigurer;
import com.github.taoroot.tao.security.captcha.CaptchaValidationRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.DelegatingOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.Resource;
import java.util.*;

@Log4j2
@Component
public class CustomSecurityConfigurer extends WebSecurityConfigurerAdapter {

    public static final String secret = "secretsecretsecretsecretsecretsecret";
    public static final String FORM_LOGIN_PATH_KEY = "/login";

    @Resource
    private CustomUserDetailsService userDetailsService;

    @Resource
    private CaptchaValidationRepository captchaValidationRepository;

    @Resource
    private ApplicationContext applicationContext;

    // @formatter:off
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CustomAuthenticationEntryPoint customAuthenticationEntryPoint = new CustomAuthenticationEntryPoint();
        CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler = new CustomAuthenticationSuccessHandler(secret);

        http
                // 验证码校验
                .apply(new CaptchaValidationConfigurer<HttpSecurity>()
                        .failureHandler(customAuthenticationEntryPoint::commence)
                        .captchaValidationRepository(captchaValidationRepository)  // 验证码存入内存
                        .smsValidationUrls(SmsCodeAuthenticationFilter.LOGIN_PATH_KEY)
                        .imageValidationUrls(FORM_LOGIN_PATH_KEY)).and() // 账号密码登录需要有图像验证码 // 手机号登录需要有手机号验证码

                // BASIC 登录
                .httpBasic(Customizer.withDefaults())

                // 表单登录
                .formLogin(config -> config.loginProcessingUrl(FORM_LOGIN_PATH_KEY)
                        .failureHandler(customAuthenticationEntryPoint::commence)
                        .successHandler(customAuthenticationSuccessHandler))

                // 社会登录
                .oauth2Login(config -> config.successHandler(new CustomOAuth2AuthenticationSuccessHandler(secret))
                        .userInfoEndpoint(userInfo -> {
                                    List<OAuth2UserService<OAuth2UserRequest, OAuth2User>> userServices = new ArrayList<>();
//                                    Map<String, Class<? extends OAuth2User>> customUserTypes = new HashMap<>();
//                                    userServices.add(new CustomUserTypesOAuth2UserService(customUserTypes));
                                    DefaultOAuth2UserService defaultOAuth2UserService = new DefaultOAuth2UserService();
                                    defaultOAuth2UserService.setRequestEntityConverter(new CustomOAuth2UserRequestEntityConverter());
                                    userServices.add(defaultOAuth2UserService);
                                    userInfo.userService(new DelegatingOAuth2UserService<>(userServices));
                                }
                        )
                        .tokenEndpoint(tokenEndpoint -> {
                            DefaultAuthorizationCodeTokenResponseClient client = new DefaultAuthorizationCodeTokenResponseClient();
                            client.setRequestEntityConverter(new CustomOAuth2AuthorizationCodeGrantRequestEntityConverter());
                            tokenEndpoint.accessTokenResponseClient(client);
                        })
                        .authorizationEndpoint(authorization ->
                                authorization.authorizationRequestRepository(new CustomHttpSessionOAuth2AuthorizationRequestRepository())
                                        .authorizationRequestResolver(new CustomOAuth2AuthorizationRequestResolver(
                                                http.getSharedObject(ApplicationContext.class).getBean(ClientRegistrationRepository.class),
                                                OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI))))

                // JWT登录
                .oauth2ResourceServer(config -> config.authenticationEntryPoint(customAuthenticationEntryPoint)
                        .jwt().decoder(new CustomJwtDecoder(secret)))

                // 手机号登录
                .apply(new SmsCodeAuthenticationConfigurer<HttpSecurity>()
                        .userDetailsService(userDetailsService)
                        .failureHandler(customAuthenticationEntryPoint::commence)
                        .successHandler(customAuthenticationSuccessHandler)).and()

                // 退出登录
                .logout(config -> config.logoutSuccessHandler(new CustomLogoutSuccessHandler()))

                // 跨域
                .cors(config -> config.configurationSource(req -> {
                    CorsConfiguration corsConfiguration = new CorsConfiguration();
                    corsConfiguration.setAllowedOrigins(Collections.singletonList("*"));
                    corsConfiguration.setAllowedHeaders(Collections.singletonList("*"));
                    corsConfiguration.setAllowedMethods(Collections.singletonList("*"));
                    return corsConfiguration;
                }))

                // 异常处理
                .exceptionHandling(config -> {
                    config.authenticationEntryPoint(customAuthenticationEntryPoint)
                            .accessDeniedHandler(new CustomAccessDeniedHandler());
                })

                // 禁用CSRF
                .csrf().disable()
                // 禁用 SESSION
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                // 设置URL权限
                .authorizeRequests(registry -> {
                    permitAllUrls(registry);  // 白名单,不需要登录也可以访问
                    registry.anyRequest().authenticated(); // 其他需要先登录再访问
                });
    }
    // @formatter:on

    public void permitAllUrls(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry) {

        List<String> permitAllUrls = new ArrayList<>(Arrays.asList(
                "/swagger-ui.html",
                "/v2/**",
                "/swagger-resources/**",
                "/webjars/**",
                "/resources/**"));
        RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();

        // 收集 NotAuth 注解的接口
        map.keySet().forEach(info -> {
            HandlerMethod handlerMethod = map.get(info);

            Set<NotAuth> set = new HashSet<>();
            set.add(AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), NotAuth.class));
            set.add(AnnotationUtils.findAnnotation(handlerMethod.getMethod(), NotAuth.class));
            set.forEach(annotation -> {
                Optional.ofNullable(annotation)
                        .ifPresent(inner -> info.getPatternsCondition().getPatterns()
                                .forEach(url -> permitAllUrls.add(ReUtil.replaceAll(url, "\\{(.*?)\\}", "*"))));
            });
        });

        permitAllUrls.forEach(url -> registry.antMatchers(url).permitAll());

        log.info("permit all urls: {}", permitAllUrls);
    }
}
