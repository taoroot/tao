package com.github.taoroot.tao;


import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestJwts {

    public static void main(String[] args) {
        String encode = new BCryptPasswordEncoder().encode("123456");
        System.out.println(encode);
    }

    public static String generateToken(String payloadJsonString, String secret)
            throws JOSEException {

        // Header
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
        // Payload
        Payload payload = new Payload(payloadJsonString);
        // JWS
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        // 加密器
        JWSSigner jwsSigner = new MACSigner(secret);

        // 签名
        jwsObject.sign(jwsSigner);

        // 生成token
        return jwsObject.serialize();
    }

}
