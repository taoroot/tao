package com.github.taoroot.tao.security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@EnableWebSecurity
public class CustomSecurityConfigurer extends WebSecurityConfigurerAdapter {

    // @formatter:off
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()      // 禁用CSRF
                .formLogin().disable() // 禁用表单登录
                .httpBasic().disable() // 禁用Basic登录
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 禁用 SESSION
                .and()
                .exceptionHandling()   // 检测到 AccessDeniedException, 根据用户身份执行不同处理
                    // 如果是匿名用户, 将启动authenticationEntryPoint
                    .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
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
