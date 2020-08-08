package com.github.taoroot.tao.security;

import com.github.taoroot.tao.utils.R;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * @author : zhiyi
 * Date: 2020/5/16
 */
@RestControllerAdvice
public class CustomSecurityExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    public R handler(NoHandlerFoundException e) {
        return R.errMsg("路径不存在");
    }

    @ExceptionHandler(Exception.class)
    public R<String> handler(Exception e) {
        e.printStackTrace();
        return R.errMsg(e.getMessage());
    }
}
