package cn.flizi.boot.tao.security.auth.oauth2;

import lombok.Data;

import java.util.Map;

@Data
public class WxOAuth2User implements CustomOAuth2User {
    public static final String TYPE = "wx";
    public static final String APP_ID = "appid";
    public static final String SECRET = "secret";

    private Map<String, Object> attributes;
    private String openid;
    private String nickname;
    private String headimgurl;

    @Override
    public String getName() {
        return this.openid;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public String getAvatar() {
        return headimgurl;
    }
}
