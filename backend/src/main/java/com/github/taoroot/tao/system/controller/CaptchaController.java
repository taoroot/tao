package com.github.taoroot.tao.system.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import com.github.taoroot.tao.security.annotation.NotAuth;
import com.github.taoroot.tao.security.captcha.CaptchaValidationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

@RestController
public class CaptchaController {

    @Resource
    private CaptchaValidationRepository captchaValidationRepository;

    @NotAuth
    @GetMapping(value = "/code/image/{key}")
    public void getImage(HttpServletResponse response, @PathVariable("key") String key) throws Exception {
        response.setDateHeader("Expires", 0);
        response.setHeader("cache-Control", "no-cache, must-revalidate");
        response.addHeader("cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/jpeg");
        try (ServletOutputStream out = response.getOutputStream()) {
            RandomGenerator randomGenerator = new RandomGenerator("0123456789", 4);
            LineCaptcha captcha = CaptchaUtil.createLineCaptcha(200, 100);
            captcha.setGenerator(randomGenerator);
            captcha.createCode();
            captcha.write(out);
            out.flush();
            captchaValidationRepository.putCode(key, captcha.getCode());
        }
    }
}

