package org.eea.security.authorization;

import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.utils.LiteralConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminUserAuthorizationImpl implements AdminUserAuthorization {

    /**
     * The admin user.
     */
    @Value("${eea.keycloak.admin.user}")
    private String adminUser;

    @Autowired
    private UserManagementControllerZull userManagementControllerZull;

    @Override
    public void setAdminSecurityContextAuthenticationWithJobUserRoles(TokenVO tokenVo, JobVO job) {
        String userId = (String) job.getParameters().get("userId");
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        List<ResourceAccessVO> resourceAccessVOS = this.userManagementControllerZull.getResourcesByUserId(userId);
        // ObjectAccessRoleEnum expression has the following format
        // ROLE_DATASCHEMA-1-DATA_CUSTODIAN
        List<String> resourceRoles = resourceAccessVOS.stream().map(resourceAccessVO -> {
            StringBuilder builder = new StringBuilder("ROLE_");
            return builder.append(resourceAccessVO.getResource().toString()).append("-")
                    .append(resourceAccessVO.getId()).append("-").append(resourceAccessVO.getRole())
                    .toString().toUpperCase();
        }).collect(Collectors.toList());
        resourceRoles.forEach((role -> grantedAuthorities.add(new SimpleGrantedAuthority(role))));
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(adminUser, LiteralConstants.BEARER_TOKEN + tokenVo.getAccessToken(), grantedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
