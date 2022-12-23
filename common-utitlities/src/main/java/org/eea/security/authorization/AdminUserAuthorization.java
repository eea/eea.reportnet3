package org.eea.security.authorization;

import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.ums.TokenVO;

public interface AdminUserAuthorization {

    void setAdminSecurityContextAuthenticationWithJobUserRoles(TokenVO tokenVo, JobVO job);
}
