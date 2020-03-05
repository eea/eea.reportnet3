package org.eea.validation.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;


/**
 * The Class CodeListUtilsTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class CodeListUtilsTest {

  /** The code list utils. */
  @InjectMocks
  private CodeListUtils codeListUtils;

  /**
   * Test sensitive code list validate.
   */
  @Test
  public void testSensitiveCodeListValidate() {
    Assert.assertEquals(true, codeListUtils.codeListValidate("1", "[1, 2]", true));
  }

  /**
   * Test non sensitive code list validate.
   */
  @Test
  public void testNonSensitiveCodeListValidate() {
    Assert.assertEquals(true, codeListUtils.codeListValidate("1", "[1, 2]", false));
  }

}
