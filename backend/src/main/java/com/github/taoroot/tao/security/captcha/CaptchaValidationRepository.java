package com.github.taoroot.tao.security.captcha;

public interface CaptchaValidationRepository {

    String getCode(String code);

    String putCode(String phone, String code);
}
