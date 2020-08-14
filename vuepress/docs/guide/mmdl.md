# 密码登录

[FormLoginConfigurer 源码分析](/resouce/)

## formLogin

```java
.formLogin(config -> config
        .loginProcessingUrl("/login")           // 设置登录地址
        .failureHandler(this::failureHandler)   // 登录失败处理
        .successHandler(this::successHandler))  // 登录成功处理
```

## failureHandler

```java
void onAuthenticationFailure(HttpServletRequest request, 
        HttpServletResponse response, AuthenticationException exception) {

    if (!response.isCommitted()) {

        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_OK);

        R<String> r = R.errMsg(authException.getMessage());

        response.getWriter().write(new ObjectMapper().writeValueAsString(r));
    }

}
```

## successHandler

```java
public void onAuthenticationSuccess(HttpServletRequest request, 
            HttpServletResponse response, Authentication authentication) {

    if (!response.isCommitted()) {

        CustomUserDetails principal = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sub", "" +principal.getId());
        jsonObject.put("aud", "PASS");
        jsonObject.put("exp", System.currentTimeMillis() / 1000 + 24 * 60 * 60);

        JWSObject jwsObject = new JWSObject(
            new JWSHeader(JWSAlgorithm.HS256), new Payload(jsonObject));
        jwsObject.sign(new MACSigner(secret));

        R<String> r = R.ok(jwsObject.serialize());

        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(new ObjectMapper().writeValueAsString(r));
    }
}
```

# UsernamePasswordAuthenticationFilter