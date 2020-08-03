package com.github.taoroot.tao.security.auth.password;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * copy 表单登录的账号密码登录验证过滤器, 改用从 json body 中获取
 */
public class CustomUsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    // ~ Static fields/initializers
    // =====================================================================================

    public static final String SPRING_SECURITY_FORM_USERNAME_KEY = "username";
    public static final String SPRING_SECURITY_FORM_PASSWORD_KEY = "password";

    private String usernameParameter = SPRING_SECURITY_FORM_USERNAME_KEY;
    private String passwordParameter = SPRING_SECURITY_FORM_PASSWORD_KEY;
    private boolean postOnly = true;

    // ~ Constructors
    // ===================================================================================================

    public CustomUsernamePasswordAuthenticationFilter() {
        super(new AntPathRequestMatcher("/login", "POST"));
    }

    // ~ Methods
    // ========================================================================================================

    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        if (postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("仅支持POST");
        }

        HashMap<String, String> hashMap = getBodyJSON(request);
        String username = hashMap.get(usernameParameter);
        String password = hashMap.get(passwordParameter);

        if (username == null) {
            username = "";
        }

        if (password == null) {
            password = "";
        }

        username = username.trim();

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                username, password);

        // Allow subclasses to set the "details" property
        setDetails(request, authRequest);

        return this.getAuthenticationManager().authenticate(authRequest);
    }


    /**
     * Provided so that subclasses may configure what is put into the authentication
     * request's details property.
     *
     * @param request     that an authentication request is being created for
     * @param authRequest the authentication request object that should have its details
     *                    set
     */
    protected void setDetails(HttpServletRequest request,
                              UsernamePasswordAuthenticationToken authRequest) {
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
    }

    /**
     * Sets the parameter name which will be used to obtain the username from the login
     * request.
     *
     * @param usernameParameter the parameter name. Defaults to "username".
     */
    public void setUsernameParameter(String usernameParameter) {
        Assert.hasText(usernameParameter, "Username parameter must not be empty or null");
        this.usernameParameter = usernameParameter;
    }

    /**
     * Sets the parameter name which will be used to obtain the password from the login
     * request..
     *
     * @param passwordParameter the parameter name. Defaults to "password".
     */
    public void setPasswordParameter(String passwordParameter) {
        Assert.hasText(passwordParameter, "Password parameter must not be empty or null");
        this.passwordParameter = passwordParameter;
    }

    /**
     * Defines whether only HTTP POST requests will be allowed by this filter. If set to
     * true, and an authentication request is received which is not a POST request, an
     * exception will be raised immediately and authentication will not be attempted. The
     * <tt>unsuccessfulAuthentication()</tt> method will be called as if handling a failed
     * authentication.
     * <p>
     * Defaults to <tt>true</tt> but may be overridden by subclasses.
     */
    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }

    public final String getUsernameParameter() {
        return usernameParameter;
    }

    public final String getPasswordParameter() {
        return passwordParameter;
    }

    public HashMap<String, String> getBodyJSON(HttpServletRequest request) {
        HashMap<String, String> hashMap = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        String line = null;

        try (BufferedReader reader = request.getReader()) {
            while (true) {
                try {
                    if ((line = reader.readLine()) == null) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sb.append(line);
            }
        } catch (IOException e) {
            return hashMap;
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            hashMap = mapper.readValue(sb.toString(), new TypeReference<HashMap<String, String>>() {
            });
        } catch (JsonProcessingException e) {
            return hashMap;
        }
        return hashMap;
    }
}
