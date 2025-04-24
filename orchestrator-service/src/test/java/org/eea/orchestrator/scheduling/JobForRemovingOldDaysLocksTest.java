package org.eea.orchestrator.scheduling;

import org.eea.interfaces.controller.dataset.DatasetController;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class JobForRemovingOldDaysLocksTest {

  private DatasetController datasetController;
  private JobForRemovingOldDaysLocks job;

  @Before
  public void setUp() {
    datasetController = mock(DatasetController.class);
    job = new JobForRemovingOldDaysLocks(datasetController);
  }

  @Test
  public void testDeletePreviousDayLocks_success() {
    when(datasetController.clearOldLocks()).thenReturn(5);

    job.deletePreviousDayLocks();

    verify(datasetController, times(1)).clearOldLocks();
  }

  @Test
  public void testDeletePreviousDayLocks_exceptionHandled() {
    when(datasetController.clearOldLocks()).thenThrow(new RuntimeException("DB error"));

    job.deletePreviousDayLocks();

    verify(datasetController, times(1)).clearOldLocks();
  }
}
