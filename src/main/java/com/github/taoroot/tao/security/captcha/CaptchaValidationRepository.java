package com.github.taoroot.tao.security.captcha;

public interface CaptchaValidationRepository {

    String getCode(String phone);

    void putCode(String phone, String code);
}
