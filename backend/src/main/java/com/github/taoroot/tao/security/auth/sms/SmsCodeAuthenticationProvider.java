package com.github.taoroot.tao.security.auth.sms;

import com.github.taoroot.tao.security.CustomUserDetailsService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

public class SmsCodeAuthenticationProvider implements AuthenticationProvider {

    private CustomUserDetailsService userDetailService;

    @Override
    public boolean supports(Class<?> aClass) {
        return SmsCodeAuthenticationToken.class.isAssignableFrom(aClass);
    }

    @Override
    public Authentication authenticate(Authentication authentication) {

        SmsCodeAuthenticationToken authenticationToken = (SmsCodeAuthenticationToken) authentication;
        String phone = (String) authenticationToken.getPrincipal();

        UserDetails userDetails = userDetailService.loadUserByPhone(phone);

        SmsCodeAuthenticationToken authenticationResult = new SmsCodeAuthenticationToken(userDetails, userDetails.getAuthorities());

        authenticationResult.setDetails(authenticationToken.getDetails());

        return authenticationResult;
    }

    public void setUserDetailService(CustomUserDetailsService userDetailService) {
        this.userDetailService = userDetailService;
    }

}
