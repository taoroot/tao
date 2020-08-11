package com.github.taoroot.tao.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.text.ParseException;
import java.util.*;

public class CustomJwtDecoder implements JwtDecoder {

    private final String secret;

    public String getSecret() {
        return secret;
    }

    public CustomJwtDecoder(String secret) {
        this.secret = secret;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            JWT parsedJwt = JWTParser.parse(token);
            JWSObject parse = JWSObject.parse(token);
            boolean verify = parse.verify(new MACVerifier(secret));

            if (!verify) {
                throw new JwtException("TOKEN不合法");
            }

            Map<String, Object> headers = new LinkedHashMap<>(parsedJwt.getHeader().toJSONObject());
            Map<String, Object> claims = parsedJwt.getJWTClaimsSet().getClaims();

            Date exp = (Date) claims.get("exp");

            if (exp == null) {
                throw new JwtException("TOKEN不合法");
            }

            if (exp.before(new Date())) {
                throw new JwtException("token过期");
            }
            HashMap<String, Object> claims1 = new HashMap<>(claims);
            claims1.put("exp", exp.toInstant());

            return Jwt.withTokenValue(token)
                    .headers(h -> h.putAll(headers))
                    .claims(c -> c.putAll(claims1))
                    .build();
        } catch (ParseException | JOSEException e) {
            throw new JwtException(e.getMessage(), e.getCause());
        }
    }
}
