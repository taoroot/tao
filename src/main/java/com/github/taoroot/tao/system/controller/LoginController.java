package com.github.taoroot.tao.system.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class LoginController {


    public ResponseEntity<String> login() {
        return new ResponseEntity<>("hello", HttpStatus.OK);
    }
}
