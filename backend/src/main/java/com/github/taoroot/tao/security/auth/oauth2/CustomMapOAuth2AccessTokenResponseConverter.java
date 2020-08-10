package com.github.taoroot.tao.security.auth.oauth2;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 解决微信问题: 解析微信获取token 接口没有 TokenType 问题
 */
public final class CustomMapOAuth2AccessTokenResponseConverter implements Converter<Map<String, String>, OAuth2AccessTokenResponse> {
    private static final Set<String> TOKEN_RESPONSE_PARAMETER_NAMES = new HashSet<>(Arrays.asList(
            OAuth2ParameterNames.ACCESS_TOKEN,
            OAuth2ParameterNames.EXPIRES_IN,
            OAuth2ParameterNames.REFRESH_TOKEN,
            OAuth2ParameterNames.SCOPE,
            OAuth2ParameterNames.TOKEN_TYPE
    ));

    @Override
    public OAuth2AccessTokenResponse convert(Map<String, String> tokenResponseParameters) {
        String accessToken = tokenResponseParameters.get(OAuth2ParameterNames.ACCESS_TOKEN);

        OAuth2AccessToken.TokenType accessTokenType = null;
        if (OAuth2AccessToken.TokenType.BEARER.getValue().equalsIgnoreCase(
                tokenResponseParameters.get(OAuth2ParameterNames.TOKEN_TYPE))) {
            accessTokenType = OAuth2AccessToken.TokenType.BEARER;
        } else {
            accessTokenType = OAuth2AccessToken.TokenType.BEARER; // 给一个默认值, 微信没有这个字段
        }

        long expiresIn = 0;
        if (tokenResponseParameters.containsKey(OAuth2ParameterNames.EXPIRES_IN)) {
            try {
                expiresIn = Long.parseLong(tokenResponseParameters.get(OAuth2ParameterNames.EXPIRES_IN));
            } catch (NumberFormatException ex) {
            }
        }

        Set<String> scopes = Collections.emptySet();
        if (tokenResponseParameters.containsKey(OAuth2ParameterNames.SCOPE)) {
            String scope = tokenResponseParameters.get(OAuth2ParameterNames.SCOPE);
            scopes = new HashSet<>(Arrays.asList(StringUtils.delimitedListToStringArray(scope, " ")));
        }

        String refreshToken = tokenResponseParameters.get(OAuth2ParameterNames.REFRESH_TOKEN);

        Map<String, Object> additionalParameters = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : tokenResponseParameters.entrySet()) {
            if (!TOKEN_RESPONSE_PARAMETER_NAMES.contains(entry.getKey())) {
                additionalParameters.put(entry.getKey(), entry.getValue());
            }
        }

        return OAuth2AccessTokenResponse.withToken(accessToken)
                .tokenType(accessTokenType)
                .expiresIn(expiresIn)
                .scopes(scopes)
                .refreshToken(refreshToken)
                .additionalParameters(additionalParameters)
                .build();
    }
}
