package org.eea.lock.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.lock.LockVO;
import org.eea.interfaces.vo.lock.enums.LockType;
import org.eea.lock.mapper.LockMapper;
import org.eea.lock.persistence.domain.Lock;
import org.eea.lock.persistence.repository.LockRepository;
import org.eea.lock.service.impl.LockServiceImpl;
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
 * The Class LockTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class LockServiceImplTest {

  /** The lock service impl. */
  @InjectMocks
  private LockServiceImpl lockServiceImpl;

  /** The lock repository. */
  @Mock
  private LockRepository lockRepository;

  /** The lock mapper. */
  @Mock
  private LockMapper lockMapper;

  /** The lock VO. */
  private LockVO lockVO;

  /** The lock criteria. */
  private Map<String, Object> lockCriteria;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
    lockCriteria = new HashMap<>();
    lockCriteria.put("criteria", "value");
    lockVO = new LockVO(new Timestamp(1L), "user", LockType.METHOD, -1877130660, lockCriteria);
  }

  /**
   * Creates the lock test 1.
   * 
   * @throws EEAException
   */
  @Test
  public void createLockTest1() throws EEAException {
    Mockito.when(lockMapper.classToEntity(Mockito.any())).thenReturn(new Lock());
    Mockito.when(lockRepository.saveIfAbsent(Mockito.any(), Mockito.any())).thenReturn(true);
    Assert.assertEquals(lockVO.getId(),
        lockServiceImpl.createLock(new Timestamp(1L), null, LockType.METHOD, lockCriteria).getId());
  }

  /**
   * Creates the lock test 2.
   */
  @Test
  public void createLockTest2() {
    Mockito.when(lockMapper.classToEntity(Mockito.any())).thenReturn(new Lock());
    Mockito.when(lockRepository.saveIfAbsent(Mockito.any(), Mockito.any())).thenReturn(false);
    try {
      lockServiceImpl.createLock(new Timestamp(1L), "user", LockType.METHOD, lockCriteria);
    } catch (EEAException e) {
      Assert.assertEquals("Method locked: " + lockVO, e.getMessage());
    }
  }

  /**
   * Removes the lock test.
   */
  @Test
  public void removeLockTest() {
    Mockito.when(lockRepository.deleteIfPresent(Mockito.any())).thenReturn(true);
    Assert.assertTrue(lockServiceImpl.removeLock(lockVO.getId()));
  }

  /**
   * Removes the lock by criteria test.
   */
  @Test
  public void removeLockByCriteriaTest() {
    Mockito.when(lockRepository.deleteIfPresent(Mockito.any())).thenReturn(true);
    Assert.assertTrue(lockServiceImpl.removeLockByCriteria(lockCriteria));
  }

  /**
   * Find lock test 1.
   */
  @Test
  public void findLockTest1() {
    Mockito.when(lockRepository.findById(Mockito.any())).thenReturn(Optional.of(new Lock()));
    Mockito.when(lockMapper.entityToClass(Mockito.any())).thenReturn(new LockVO());
    Assert.assertNotNull(lockServiceImpl.findById(1));
  }

  /**
   * Find lock test 2.
   */
  @Test
  public void findLockTest2() {
    Mockito.when(lockRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    Assert.assertNull(lockServiceImpl.findById(1));
  }

  /**
   * Find all test.
   */
  @Test
  public void findAllTest() {
    List<Lock> list = new ArrayList<>();
    list.add(new Lock());
    Mockito.when(lockRepository.findAll()).thenReturn(list);
    Mockito.when(lockMapper.entityToClass(Mockito.any())).thenReturn(new LockVO());
    Assert.assertFalse(lockServiceImpl.findAll().isEmpty());
  }
}
