package com.github.taoroot.tao.security.captcha;

import org.springframework.security.core.AuthenticationException;

public class CaptchaValidationException extends AuthenticationException {

    private static final long serialVersionUID = 5022575393500654459L;

    public CaptchaValidationException(String message) {
        super(message);
    }
}
