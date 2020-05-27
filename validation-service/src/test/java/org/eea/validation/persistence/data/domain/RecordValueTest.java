package org.eea.validation.persistence.data.domain;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RecordValueTest {

  @InjectMocks
  private RecordValue recordValue;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void recordIfThenTrueTrueTest() {
    Assert.assertTrue(recordValue.recordIfThen(true, true));
  }

  @Test
  public void recordIfThenTrueFalseTest() {
    Assert.assertFalse(recordValue.recordIfThen(true, false));
  }

  @Test
  public void recordIfThenFalseFalseTest() {
    Assert.assertTrue(recordValue.recordIfThen(false, true));
  }

  @Test
  public void recordIfThenFalseTrueTest() {
    Assert.assertTrue(recordValue.recordIfThen(false, false));
  }
}
