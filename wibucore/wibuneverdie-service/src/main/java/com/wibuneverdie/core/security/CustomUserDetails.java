package com.wibuneverdie.core.security;

import com.wibuneverdie.core.entity.UaUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * UserDetails wrapper cho UaUser.
 * username = user_id (dùng để đăng nhập),
 * userUid  = user_uid (dùng để sinh JWT claim và tra cứu role/menu).
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final String userUid;
    private final String username;   // = userId
    private final String password;
    private final String status;

    public CustomUserDetails(UaUser user) {
        this.userUid  = user.getUserUid();
        this.username = user.getUserId();
        this.password = user.getPwd();
        this.status   = user.getStatus();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Authorities được quản lý qua ua_role – trả về rỗng ở đây,
        // role/menu check được thực hiện qua MenuService / custom logic.
        return List.of();
    }

    @Override
    public boolean isAccountNonExpired()  { return true; }

    @Override
    public boolean isAccountNonLocked()   { return !"LOCKED".equalsIgnoreCase(status); }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled()            { return "ACTIVE".equalsIgnoreCase(status); }
}
