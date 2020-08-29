package cn.flizi.boot.tao.security;


import cn.flizi.boot.tao.security.auth.oauth2.CustomOAuth2User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface CustomUserDetailsService extends UserDetailsService {

    CustomUserDetails loadUserByPhone(String phone) throws UsernameNotFoundException;

    CustomUserDetails loadUserByOAuth2(String clientId, CustomOAuth2User oAuth2User);

    String bindOauth2(String clientId, CustomOAuth2User principal, Integer userId);

    @Override
    CustomUserDetails loadUserByUsername(String username);

    CustomUserDetails loadUserById(String userId);

    default CustomUserDetails loadUserById(Integer userId) {
        return loadUserById(String.format("%d", userId));
    }
}
