package cn.flizi.boot.tao.system.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.flizi.boot.tao.security.annotation.NotAuth;
import cn.flizi.boot.tao.security.captcha.CaptchaValidationRepository;
import cn.flizi.boot.tao.utils.R;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

@RestController
public class SysCaptchaController {

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

    @NotAuth
    @GetMapping(value = "/code/sms")
    public R getSMS(@RequestParam("phone") String key)  {
        if (StringUtils.isEmpty(key) || key.length() < 10) {
            return R.errMsg("手机号不合法");
        }

       captchaValidationRepository.putCode(key, "1234");
       return R.okMsg("发送信息到手机号: " + key + " ====> " + ", 验证码: 1234");
    }
}

