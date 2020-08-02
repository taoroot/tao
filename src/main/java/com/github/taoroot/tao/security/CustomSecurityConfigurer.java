package com.github.taoroot.tao.security;

import com.github.taoroot.tao.security.auth.password.CustomUsernamePasswordSecurityConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

@EnableWebSecurity
public class CustomSecurityConfigurer extends WebSecurityConfigurerAdapter {

    public static final String secret = "123123123123123123123123123123123123123123123123123123123123123";

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        super.configure(auth);
    }

    // @formatter:off
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CustomAuthenticationEntryPoint customAuthenticationEntryPoint = new CustomAuthenticationEntryPoint();

        // 自定义密码登录器
        http.apply(new CustomUsernamePasswordSecurityConfigurer<>(
                authenticationManagerBean(),
                userDetailsService(),
                new CustomAuthenticationSuccessHandler(secret)));

        // 自定义JWT登录器
        http.oauth2ResourceServer()
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                .jwt()
                .decoder(new CustomJwtDecoder(secret));

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
    public UserDetailsService userDetailsService() {
        UserDetails userDetails = User.withDefaultPasswordEncoder()
                .username("user")
                .password("password")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(userDetails);
    }
}
