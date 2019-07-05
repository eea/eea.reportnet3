package org.eea.security.jwt.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Setter
@ToString
public class EeaUserDetails implements UserDetails {

  private static final long serialVersionUID = 5374139621703446667L;

  private String username;
  private String password;
  private Collection<? extends GrantedAuthority> authorities;

  public static EeaUserDetails create(String username, String password) {
    EeaUserDetails principal = new EeaUserDetails();
    principal.setUsername(username);
    principal.setPassword(username);
    List<GrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_PROVIDER"));
    authorities.add(new SimpleGrantedAuthority("ROLE_STEWARD"));
    authorities.add(new SimpleGrantedAuthority("ROLE_REQUESTOR"));
    principal.setAuthorities(authorities);

    return principal;
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
    return true;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EeaUserDetails principal = (EeaUserDetails) o;
    return username.equals(principal.username);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username);
  }
}
