package com.github.taoroot.tao.security.auth.sms;

public interface SmsCodeValidationRepository {

    String getCode(String phone);

    void putCode(String phone, String code);
}
