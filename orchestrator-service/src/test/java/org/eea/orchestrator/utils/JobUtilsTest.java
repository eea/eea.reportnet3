package org.eea.orchestrator.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eea.interfaces.vo.orchestrator.JobHistoryVO;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class JobUtilsTest {

  private JobUtils jobUtils;

  @Before
  public void setUp() {
    jobUtils = new JobUtils();
  }

  @After
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void authenticate(String username, String... roles) {
    List<SimpleGrantedAuthority> authorities = Arrays.stream(roles)
        .map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(username, null, authorities));
  }

  private List<JobVO> jobsCreatedBy(String... usernames) {
    List<JobVO> jobs = new ArrayList<>();
    for (String username : usernames) {
      JobVO job = new JobVO();
      job.setCreatorUsername(username);
      jobs.add(job);
    }
    return jobs;
  }

  private void hide(List<JobVO> jobs) {
    jobUtils.hideOtherCreatorUsernames(jobs, JobVO::getCreatorUsername, JobVO::setCreatorUsername);
  }

  @Test
  public void adminCustodianAndStewardSeeAllUsernames() {
    for (String role : Arrays.asList("ROLE_ADMIN", "ROLE_DATA_CUSTODIAN", "ROLE_DATA_STEWARD")) {
      authenticate("user1", role);
      List<JobVO> jobs = jobsCreatedBy("user1", "user2");

      hide(jobs);

      assertTrue(jobUtils.canSeeAllCreatorUsernames());
      assertEquals("user1", jobs.get(0).getCreatorUsername());
      assertEquals("user2", jobs.get(1).getCreatorUsername());
    }
  }

  @Test
  public void otherRolesSeeOnlyTheirOwnUsername() {
    authenticate("user1", "ROLE_LEAD_REPORTER", "ROLE_DATAFLOW-1-LEAD_REPORTER");
    List<JobVO> jobs = jobsCreatedBy("user1", "user2", null);

    hide(jobs);

    assertFalse(jobUtils.canSeeAllCreatorUsernames());
    assertEquals("user1", jobs.get(0).getCreatorUsername());
    assertNull(jobs.get(1).getCreatorUsername());
    assertNull(jobs.get(2).getCreatorUsername());
  }

  @Test
  public void dataflowLevelCustodianRoleDoesNotGrantVisibility() {
    authenticate("user1", "ROLE_DATAFLOW-1-DATA_CUSTODIAN");
    List<JobVO> jobs = jobsCreatedBy("user2");

    hide(jobs);

    assertNull(jobs.get(0).getCreatorUsername());
  }

  @Test
  public void noAuthenticationHidesAllUsernames() {
    List<JobVO> jobs = jobsCreatedBy("user1", "user2");

    hide(jobs);

    assertFalse(jobUtils.canSeeAllCreatorUsernames());
    assertNull(jobs.get(0).getCreatorUsername());
    assertNull(jobs.get(1).getCreatorUsername());
  }

  @Test
  public void emptyAndNullListsAreIgnored() {
    authenticate("user1", "ROLE_LEAD_REPORTER");

    hide(Collections.emptyList());
    hide(null);
  }

  @Test
  public void hiddenUsernameIsOmittedFromJson() throws Exception {
    authenticate("user1", "ROLE_LEAD_REPORTER");
    List<JobVO> jobs = jobsCreatedBy("user1", "user2");

    hide(jobs);

    ObjectMapper mapper = new ObjectMapper();
    assertEquals("user1",
        mapper.readTree(mapper.writeValueAsString(jobs.get(0))).get("creatorUsername").asText());
    assertFalse(mapper.readTree(mapper.writeValueAsString(jobs.get(1))).has("creatorUsername"));
  }

  @Test
  public void hiddenUsernameIsOmittedFromJobHistoryJson() throws Exception {
    authenticate("user1", "ROLE_LEAD_REPORTER");
    List<JobHistoryVO> jobHistories = new ArrayList<>();
    for (String username : Arrays.asList("user1", "user2")) {
      JobHistoryVO jobHistory = new JobHistoryVO();
      jobHistory.setCreatorUsername(username);
      jobHistories.add(jobHistory);
    }

    jobUtils.hideOtherCreatorUsernames(jobHistories, JobHistoryVO::getCreatorUsername,
        JobHistoryVO::setCreatorUsername);

    ObjectMapper mapper = new ObjectMapper();
    assertEquals("user1",
        mapper.readTree(mapper.writeValueAsString(jobHistories.get(0))).get("creatorUsername").asText());
    assertFalse(mapper.readTree(mapper.writeValueAsString(jobHistories.get(1))).has("creatorUsername"));
  }
}