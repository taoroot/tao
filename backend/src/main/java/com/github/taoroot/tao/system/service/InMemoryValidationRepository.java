package com.github.taoroot.tao.system.service;

import cn.hutool.cache.impl.TimedCache;
import com.github.taoroot.tao.security.captcha.CaptchaValidationRepository;
import org.springframework.stereotype.Component;

@Component
public class InMemoryValidationRepository implements CaptchaValidationRepository {

    private static final TimedCache<String, String> cache = new TimedCache<>(1000 * 60 * 5);

    @Override
    public String getCode(String code) {
        return cache.get(code);
    }

    @Override
    public String putCode(String phone, String code) {
        cache.put(phone, code);
        return phone;
    }
}
