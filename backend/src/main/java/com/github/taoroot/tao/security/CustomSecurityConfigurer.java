package com.github.taoroot.tao.security;

import cn.hutool.core.util.ReUtil;
import com.github.taoroot.tao.security.annotation.NotAuth;
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

        // Basic 登录
        http
                .apply(new CaptchaValidationConfigurer<HttpSecurity>() // 验证码校验
                        .failureHandler(customAuthenticationEntryPoint::commence)
                        .captchaValidationRepository(captchaValidationRepository)  // 验证码存入内存
                        .smsValidationUrls(SmsCodeAuthenticationFilter.LOGIN_PATH_KEY)
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
                .authorizeRequests(registry -> {
                    permitAllUrls(registry);  // 白名单
                    registry.anyRequest().authenticated(); // 其他需要验证
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
