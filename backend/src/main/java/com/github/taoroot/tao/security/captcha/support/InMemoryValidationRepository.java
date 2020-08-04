package com.github.taoroot.tao.security.captcha.support;

import com.github.taoroot.tao.security.captcha.CaptchaValidationRepository;

import java.util.HashMap;

public class InMemoryValidationRepository implements CaptchaValidationRepository {

    private HashMap<String, String> hashMap = new HashMap<>();

    @Override
    public String getCode(String code) {
        return hashMap.get(code);
    }

    @Override
    public void putCode(String phone, String code) {
        hashMap.put(phone, code);
    }
}
