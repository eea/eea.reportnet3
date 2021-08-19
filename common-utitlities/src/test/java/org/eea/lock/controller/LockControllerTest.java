package org.eea.lock.controller;

import static org.mockito.Mockito.times;

import java.util.ArrayList;
import org.eea.interfaces.vo.lock.LockVO;
import org.eea.lock.service.LockService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class LockControllerTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class LockControllerTest {

  /** The lock controller. */
  @InjectMocks
  private LockController lockController;

  /** The lock service. */
  @Mock
  private LockService lockService;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Removes the lock test.
   */
  @Test
  public void removeLockTest() {
    Mockito.when(lockService.removeLock(Mockito.any())).thenReturn(true);
    lockController.removeLock(1);
    Mockito.verify(lockService, times(1)).removeLock(Mockito.any());
  }

  /**
   * Find all locks test.
   */
  @Test
  public void findAllLocksTest() {
    Mockito.when(lockService.findAll()).thenReturn(new ArrayList<LockVO>());
    Assert.assertTrue(lockController.findAllLocks().isEmpty());
  }

  /**
   * Find one lock test.
   */
  @Test
  public void findOneLockTest() {
    Mockito.when(lockService.findById(Mockito.any())).thenReturn(new LockVO());
    Assert.assertNotNull(lockController.findOneLock(1));
  }
}
