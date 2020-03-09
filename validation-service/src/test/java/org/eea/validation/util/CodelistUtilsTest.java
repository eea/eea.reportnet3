/*
 * 
 */
package org.eea.validation.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;


/**
 * The Class CodelistUtilsTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class CodelistUtilsTest {

  /** The code list utils. */
  @InjectMocks
  private CodelistUtils codelistUtils;

  /**
   * Test sensitive code list validate.
   */
  @Test
  public void testSensitiveCodelistValidate() {
    // Assert.assertEquals(true, codelistUtils.codelistValidate("1", "[1, 2]", true));
  }

  /**
   * Test non sensitive code list validate.
   */
  @Test
  public void testNonSensitiveCodelistValidate() {
    // Assert.assertEquals(true, codelistUtils.codelistValidate("1", "[1, 2]", false));
  }

}
