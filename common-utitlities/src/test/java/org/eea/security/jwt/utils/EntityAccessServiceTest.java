package org.eea.security.jwt.utils;

import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.vo.enums.EntityClassEnum;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * The Class EntityAccessServiceTest.
 */
public class EntityAccessServiceTest {

  /** The entity access service. */
  @InjectMocks
  private EntityAccessService entityAccessService;

  /** The dataflow controller zuul. */
  @Mock
  private DataFlowControllerZuul dataflowControllerZuul;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Checks if is reference dataflow draft test.
   */
  @Test
  public void isReferenceDataflowDraftTest() {
    Assert.assertFalse(entityAccessService.isReferenceDataflowDraft(EntityClassEnum.DATAFLOW, 1L));
  }


}
