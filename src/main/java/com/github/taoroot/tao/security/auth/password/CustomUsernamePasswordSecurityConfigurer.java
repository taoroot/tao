package com.github.taoroot.tao.security.auth.password;

import com.github.taoroot.tao.security.CustomAuthenticationSuccessHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 没啥用,先放着
 *
 * @param <H>
 */
public final class CustomUsernamePasswordSecurityConfigurer<H extends HttpSecurityBuilder<H>>
        extends AbstractHttpConfigurer<CustomUsernamePasswordSecurityConfigurer<H>, H> {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final CustomAuthenticationSuccessHandler successHandler;

    public CustomUsernamePasswordSecurityConfigurer(AuthenticationManager authenticationManager,
                                                    UserDetailsService userDetailsService,
                                                    CustomAuthenticationSuccessHandler successHandler) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.successHandler = successHandler;
    }

    @Override
    public void configure(H builder) throws Exception {
        super.configure(builder);

        // 添加账号密码认证器
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        PasswordEncoder passwordEncoder = builder.getSharedObject(PasswordEncoder.class);
        if (passwordEncoder != null) {
            provider.setPasswordEncoder(passwordEncoder);
        }
        provider.afterPropertiesSet();
        builder.authenticationProvider(provider);


        // 自定义用户密码过滤器
        CustomUsernamePasswordAuthenticationFilter filter = new CustomUsernamePasswordAuthenticationFilter();
        filter.setAuthenticationManager(authenticationManager);
        filter.setAuthenticationSuccessHandler(successHandler);

        builder.addFilterBefore(filter,
                UsernamePasswordAuthenticationFilter.class);
    }
}
