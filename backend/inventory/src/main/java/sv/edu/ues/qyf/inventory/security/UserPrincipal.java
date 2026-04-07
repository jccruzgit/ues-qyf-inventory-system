package sv.edu.ues.qyf.inventory.security;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import sv.edu.ues.qyf.inventory.entity.AccessScope;
import sv.edu.ues.qyf.inventory.entity.User;

public class UserPrincipal implements UserDetails {

    private final String username;
    private final String password;
    private final String fullName;
    private final String role;
    private final AccessScope accessScope;
    private final boolean enabled;
    private final List<GrantedAuthority> authorities;

    public UserPrincipal(User user) {
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.fullName = user.getFullName();
        this.role = user.getRole().getName();
        this.accessScope = user.getAccessScope();
        this.enabled = Boolean.TRUE.equals(user.getActive());
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }

    public AccessScope getAccessScope() {
        return accessScope;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
