package com.github.taoroot.tao.security.auth.sms;

import com.github.taoroot.tao.security.auth.RequestUtil;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

public class SmsCodeAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String PHONE_KEY = "phone";

    private boolean postOnly = true;

    SmsCodeAuthenticationFilter() {
        super(new AntPathRequestMatcher("/login/phone", HttpMethod.POST.toString()));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {

        if (postOnly && !request.getMethod().equals(HttpMethod.POST.toString())) {
            throw new AuthenticationServiceException("仅支持POST");
        }

        HashMap<String, String> bodyJSON = RequestUtil.getBodyJSON(request);
        String phone = bodyJSON.get(PHONE_KEY);

        if (phone == null) {
            phone = "";
        }

        phone = phone.trim();

        SmsCodeAuthenticationToken authRequest = new SmsCodeAuthenticationToken(phone);

        setDetails(request, authRequest);

        return this.getAuthenticationManager().authenticate(authRequest);
    }


    private void setDetails(HttpServletRequest request, SmsCodeAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }
}
