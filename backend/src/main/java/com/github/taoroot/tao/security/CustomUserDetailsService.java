package com.github.taoroot.tao.security;


import com.github.taoroot.tao.security.auth.oauth2.CustomOAuth2User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface CustomUserDetailsService extends UserDetailsService {

    /**
     * Create a new user with the supplied details.
     */
    CustomUserDetails createUser(CustomUserDetails user);


    /**
     * Update the specified user.
     */
    void updateUser(CustomUserDetails user);

    /**
     * Remove the user with the given login name from the system.
     */
    void deleteUser(String username);

    /**
     * Modify the current user's password. This should change the user's password in the
     * persistent user repository (datbase, LDAP etc).
     *
     * @param oldPassword current password (for re-authentication if required)
     * @param newPassword the password to change to
     */
    void changePassword(String oldPassword, String newPassword);

    /**
     * Check if a user with the supplied login name exists in the system.
     */
    boolean userExists(String username);

    /**
     * 通过手机号登录
     */
    CustomUserDetails loadUserByPhone(String phone) throws UsernameNotFoundException;

    CustomUserDetails loadUserByOAuth2(String clientId, CustomOAuth2User oAuth2User, boolean create);

    String bindOauth2(String clientId, CustomOAuth2User principal, Integer userId);

    @Override
    CustomUserDetails loadUserByUsername(String username);

    CustomUserDetails loadUserById(String userId);

    default CustomUserDetails loadUserById(Integer userId) {
        return loadUserById(String.format("%d", userId));
    }
}
