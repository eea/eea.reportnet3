package org.eea.security.jwt.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.security.jwt.data.TokenDataVO;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthenticationUtils {

  public static void performAuthentication(TokenDataVO token, String credentials) {
    String username = token.getPreferredUsername();
    Map<String, Object> otherClaims = token.getOtherClaims();

    Set<String> roles = token.getRoles();
    List<String> groups = (List<String>) otherClaims.get("user_groups");
    if (null != groups && !groups.isEmpty()) {
      groups.stream().map(group -> {
        if (group.startsWith("/")) {
          group = group.substring(1);
        }
        return group.toUpperCase();
      }).forEach(roles::add);
    }
    UserDetails userDetails = EeaUserDetails.create(username, roles);
    // Adding again the toke type so it can be used in EeaFeignSecurityInterceptor regardless
    // the token type
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(userDetails, credentials,
            userDetails.getAuthorities());
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, token.getUserId());
    authentication.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  public static TokenDataVO tokenVO2TokenDataVO(TokenVO tokenVO) {
    TokenDataVO tokenDataVO = new TokenDataVO();
    tokenDataVO.setRoles(tokenVO.getRoles());
    Map<String, Object> userGroupsMap = new HashMap<>();
    List<String> userGroups = new ArrayList<>(tokenVO.getGroups());
    userGroupsMap.put("user_groups", userGroups);
    tokenDataVO.setOtherClaims(userGroupsMap);
    tokenDataVO.setUserId(tokenVO.getUserId());
    tokenDataVO.setPreferredUsername(tokenVO.getPreferredUsername());
    return tokenDataVO;
  }
}
