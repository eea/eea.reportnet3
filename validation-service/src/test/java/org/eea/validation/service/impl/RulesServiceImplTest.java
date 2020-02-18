package org.eea.validation.service.impl;

import org.eea.exception.EEAException;
import org.eea.validation.persistence.repository.RulesRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class RulesServiceImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class RulesServiceImplTest {

  /** The rules service impl. */
  @InjectMocks
  private RulesServiceImpl rulesServiceImpl;

  /** The rules repository. */
  @Mock
  private RulesRepository rulesRepository;

  /**
   * Delete rule by id.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteRuleById() throws EEAException {
    rulesServiceImpl.deleteRuleById("ObjectId", "ObjectId");

  }

  /**
   * Delete rule by reference id.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteRuleByReferenceId() throws EEAException {
    rulesServiceImpl.deleteRuleByReferenceId("ObjectId", "ObjectId");

  }
}
