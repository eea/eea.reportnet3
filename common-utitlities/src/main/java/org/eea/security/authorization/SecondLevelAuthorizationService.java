package org.eea.security.authorization;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecondLevelAuthorizationService {

  public boolean checkObjectAccess(Long idEntity, ObjectAccessRoleEnum... objectAccess) {
    Collection<String> authorities = SecurityContextHolder.getContext()
        .getAuthentication()
        .getAuthorities().stream().map(authority -> ((GrantedAuthority) authority).getAuthority())
        .collect(
            Collectors.toList());
    List<String> roles = Arrays.asList(objectAccess).stream()
        .map(objectAccessRoleEnum -> objectAccessRoleEnum.getAccessRole(idEntity)).collect(
            Collectors.toList());

    return !roles.stream().filter(authorities::contains).findFirst().orElse("not_found")
        .equals("not_found");
  }
}
