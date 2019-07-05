package org.eea.ums.service;

import org.eea.ums.utils.EeaUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * The type Eea user details service.
 */
public class EeaUserDetailsService implements UserDetailsService {


  @Override
  public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
    UserDetails principal = null;
    if (userName.equals("eea")) {
      principal = EeaUserDetails.create(userName, "");
    } else {
      throw new UsernameNotFoundException(String.format("User name %s not found", userName));
    }

    return principal;
  }
}
