package org.eea.recordstore.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doThrow;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.recordstore.exception.RecordStoreAccessException;
import org.eea.recordstore.service.RecordStoreService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class SnapshotHelperTest {

  @InjectMocks
  private SnapshotHelper snapshotHelper;

  private ExecutorService restorationExecutorService;

  @Mock
  private RecordStoreService recordStoreService;

  @Before
  public void initMocks() {
    restorationExecutorService = Executors.newFixedThreadPool(2);
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void finishTasks() {
    restorationExecutorService.shutdown();
  }

  @Test
  public void processRestorationTest() throws EEAException, SQLException, IOException,
      RecordStoreAccessException, InterruptedException {
    ReflectionTestUtils.setField(snapshotHelper, "restorationExecutorService",
        Executors.newFixedThreadPool(2));
    ReflectionTestUtils.setField(snapshotHelper, "maxRunningTasks", 2);
    snapshotHelper.processRestoration(1L, 1L, 1L, DatasetTypeEnum.DESIGN, "user", true, true);

    Thread.sleep(1000);
    Mockito.verify(recordStoreService, Mockito.times(1)).restoreDataSnapshot(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
  }

  @Test
  public void processRestorationMaximunTasksTest() throws EEAException, SQLException, IOException,
      RecordStoreAccessException, InterruptedException {
    ReflectionTestUtils.setField(snapshotHelper, "restorationExecutorService",
        Executors.newFixedThreadPool(1));
    ReflectionTestUtils.setField(snapshotHelper, "maxRunningTasks", 1);
    snapshotHelper.processRestoration(1L, 1L, 1L, DatasetTypeEnum.DESIGN, "user", true, true);
    snapshotHelper.processRestoration(1L, 1L, 1L, DatasetTypeEnum.DESIGN, "user", true, true);

    Thread.sleep(1000);
    Mockito.verify(recordStoreService, Mockito.times(2)).restoreDataSnapshot(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
  }

  @Test
  public void processRestorationExceptionTest() throws EEAException, SQLException, IOException,
      RecordStoreAccessException, InterruptedException {
    ReflectionTestUtils.setField(snapshotHelper, "restorationExecutorService",
        Executors.newFixedThreadPool(2));
    ReflectionTestUtils.setField(snapshotHelper, "maxRunningTasks", 2);
    doThrow(new SQLException()).when(recordStoreService).restoreDataSnapshot(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    snapshotHelper.processRestoration(1L, 1L, 1L, DatasetTypeEnum.DESIGN, "user", true, true);
    Thread.sleep(1000);
    Mockito.verify(recordStoreService, Mockito.times(1)).restoreDataSnapshot(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
  }

  @Test
  public void destroyTest() throws Exception {
    ReflectionTestUtils.setField(snapshotHelper, "restorationExecutorService",
        Executors.newFixedThreadPool(2));
    snapshotHelper.destroy();
    ExecutorService z = (ExecutorService) ReflectionTestUtils.getField(snapshotHelper,
        "restorationExecutorService");
    // Thread.sleep(1000);
    assertNotNull(z);
  }
}
