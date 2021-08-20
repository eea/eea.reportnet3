package org.eea.recordstore.util;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.eea.exception.EEAException;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.recordstore.exception.RecordStoreAccessException;
import org.eea.recordstore.service.RecordStoreService;
import org.eea.security.jwt.utils.EeaUserDetails;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * The Class ViewHelperTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ViewHelperTest {

  /**
   * The view helper.
   */
  @InjectMocks
  private ViewHelper viewHelper;

  /**
   * The view executor service.
   */
  private ExecutorService viewExecutorService;

  /**
   * The processes map.
   */
  private List<Long> processesList;

  /**
   * The record store service.
   */
  @Mock
  private RecordStoreService recordStoreService;


  /**
   * The kafka sender utils.
   */
  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    viewExecutorService = Executors.newFixedThreadPool(2);
    processesList = new ArrayList<>();
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Finish tasks.
   */
  @After
  public void finishTasks() {
    viewExecutorService.shutdown();
  }

  /**
   * Insert view procces one active test.
   *
   * @throws EEAException the EEA exception
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws RecordStoreAccessException the record store access exception
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void insertViewProccesOneActiveTest() throws EEAException, SQLException, IOException,
      RecordStoreAccessException, InterruptedException {
    ReflectionTestUtils.setField(viewHelper, "viewExecutorService",
        Executors.newFixedThreadPool(2));
    ReflectionTestUtils.setField(viewHelper, "maxRunningTasks", 2);
    processesList.add(1L);
    ReflectionTestUtils.setField(viewHelper, "processesList", processesList);
    viewHelper.insertViewProcces(1L, true, true);
    Thread.interrupted();
    TimeUnit.SECONDS.sleep(1);
    Mockito.verify(kafkaSenderUtils, Mockito.times(1)).releaseDatasetKafkaEvent(Mockito.any(),
        Mockito.any());
    Thread.currentThread().interrupt();
  }

  /**
   * Insert view procces zero active test.
   *
   * @throws EEAException the EEA exception
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws RecordStoreAccessException the record store access exception
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void insertViewProccesZeroActiveTest() throws EEAException, SQLException, IOException,
      RecordStoreAccessException, InterruptedException {
    ReflectionTestUtils.setField(viewHelper, "viewExecutorService",
        Executors.newFixedThreadPool(2));
    ReflectionTestUtils.setField(viewHelper, "maxRunningTasks", 2);
    processesList.add(0L);
    ReflectionTestUtils.setField(viewHelper, "processesList", processesList);
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(
            EeaUserDetails.create("user", new HashSet<>()),
            "token", null));
    viewHelper.insertViewProcces(1L, true, true);
    Thread.interrupted();
    TimeUnit.SECONDS.sleep(1);
    Mockito.verify(kafkaSenderUtils, Mockito.times(1)).releaseDatasetKafkaEvent(Mockito.any(),
        Mockito.any());
    Thread.currentThread().interrupt();
  }

  /**
   * Destroy test.
   *
   * @throws Exception the exception
   */
  @Test
  public void destroyTest() throws Exception {
    ReflectionTestUtils.setField(viewHelper, "viewExecutorService",
        Executors.newFixedThreadPool(2));
    viewHelper.destroy();
    ExecutorService z =
        (ExecutorService) ReflectionTestUtils.getField(viewHelper, "viewExecutorService");
    assertNotNull(z);
  }

  /**
   * Finish procces test.
   *
   * @throws EEAException the EEA exception
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws RecordStoreAccessException the record store access exception
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void finishProccesTest() throws EEAException, SQLException, IOException,
      RecordStoreAccessException, InterruptedException {
    ReflectionTestUtils.setField(viewHelper, "viewExecutorService",
        Executors.newFixedThreadPool(2));
    ReflectionTestUtils.setField(viewHelper, "maxRunningTasks", 2);
    processesList.add(1L);
    processesList.add(1L);
    ReflectionTestUtils.setField(viewHelper, "processesList", processesList);
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(
            EeaUserDetails.create("user", new HashSet<>()),
            "token", null));

    viewHelper.finishProcces(1L, true, true);
    Thread.interrupted();
    TimeUnit.SECONDS.sleep(1);
    Mockito.verify(kafkaSenderUtils, Mockito.times(1)).releaseDatasetKafkaEvent(Mockito.any(),
        Mockito.any());
    Thread.currentThread().interrupt();
  }

  /**
   * Delete procces list test.
   */
  @Test
  public void deleteProccesListTest() {
    processesList.add(1L);
    ReflectionTestUtils.setField(viewHelper, "processesList", processesList);
    viewHelper.deleteProccesList(1L);
    Assert.assertEquals(0, processesList.size());
  }

  /**
   * Insert procces list test.
   */
  @Test
  public void insertProccesListTest() {
    processesList.add(1L);
    ReflectionTestUtils.setField(viewHelper, "processesList", processesList);
    viewHelper.insertProccesList(1L);
    Assert.assertEquals(2, processesList.size());
  }

}
