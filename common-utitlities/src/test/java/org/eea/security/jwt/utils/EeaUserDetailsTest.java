package org.eea.security.jwt.utils;

import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class EeaUserDetailsTest {


  @Test
  public void createProvider() {
    Set<String> roles = new HashSet<>();
    roles.add("DATA_PROVIDER");
    EeaUserDetails result = EeaUserDetails.create("user1", roles);
    Assert.assertNotNull(result);
    Assert.assertNotNull(result.getUsername());
    Assert.assertNotNull(result.getAuthorities());
    Assert.assertNotNull(result.getAuthorities().iterator().next());
    Assert.assertEquals("ROLE_DATA_PROVIDER",
        result.getAuthorities().iterator().next().getAuthority());


  }

  @Test
  public void createCustodian() {
    Set<String> roles = new HashSet<>();
    roles.add("ROLE_DATA_CUSTODIAN");
    EeaUserDetails result = EeaUserDetails.create("user1", roles);
    Assert.assertNotNull(result);
    Assert.assertNotNull(result.getUsername());
    Assert.assertNotNull(result.getAuthorities());
    Assert.assertNotNull(result.getAuthorities().iterator().next());
    Assert.assertEquals("ROLE_DATA_CUSTODIAN",
        result.getAuthorities().iterator().next().getAuthority());
  }
}
