package com.github.taoroot.tao.security.auth.sms;

import org.springframework.security.core.AuthenticationException;

public class SmsCodeValidateException extends AuthenticationException {

    private static final long serialVersionUID = 5022575393500654459L;

    public SmsCodeValidateException(String message) {
        super(message);
    }
}
