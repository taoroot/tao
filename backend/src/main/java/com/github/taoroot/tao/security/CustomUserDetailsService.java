package com.github.taoroot.tao.security;


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

    CustomUserDetails loadUserByOAuth(String clientId, String name, boolean create);
    String bindOauth2(String clientId, String name, Integer userId);

    @Override
    CustomUserDetails loadUserByUsername(String username);
}
