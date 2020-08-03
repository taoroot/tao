package com.github.taoroot.tao.security.auth.sms.support;

import com.github.taoroot.tao.security.auth.sms.SmsCodeValidateRepository;

public class InMemorySmsCodeValidateRepository implements SmsCodeValidateRepository {

    @Override
    public String getCode(String code) {
        return null;
    }

    @Override
    public void putCode(String phone, String code) {

    }
}
