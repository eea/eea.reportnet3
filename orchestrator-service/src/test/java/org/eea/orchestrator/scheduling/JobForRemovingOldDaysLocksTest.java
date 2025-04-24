package org.eea.orchestrator.scheduling;

import org.eea.lock.service.LockService;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class JobForRemovingOldDaysLocksTest {

  private LockService lockService;
  private JobForRemovingOldDaysLocks job;

  @Before
  public void setUp() {
    lockService = mock(LockService.class);
    job = new JobForRemovingOldDaysLocks(lockService);
  }

  @Test
  public void testDeletePreviousDayLocks_success() {
    when(lockService.deletePreviousDayLocks()).thenReturn(5);

    job.deletePreviousDayLocks();

    verify(lockService, times(1)).deletePreviousDayLocks();
  }

  @Test
  public void testDeletePreviousDayLocks_exceptionHandled() {
    when(lockService.deletePreviousDayLocks()).thenThrow(new RuntimeException("DB error"));

    job.deletePreviousDayLocks();

    verify(lockService, times(1)).deletePreviousDayLocks();
  }
}
