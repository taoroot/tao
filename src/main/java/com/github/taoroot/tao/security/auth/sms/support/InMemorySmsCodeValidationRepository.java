package com.github.taoroot.tao.security.auth.sms.support;

import com.github.taoroot.tao.security.auth.sms.SmsCodeValidationRepository;

public class InMemorySmsCodeValidationRepository implements SmsCodeValidationRepository {

    @Override
    public String getCode(String code) {
        return null;
    }

    @Override
    public void putCode(String phone, String code) {

    }
}
