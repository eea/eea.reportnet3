package org.eea.validation.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class CodeListUtilsTest {

  @InjectMocks
  private CodeListUtils codeListUtils;

  @Test
  public void testSensitiveCodeListValidate() {
    codeListUtils.codeListValidate("1", "[1, 2]", true);
  }

  @Test
  public void testNonSensitiveCodeListValidate() {

    codeListUtils.codeListValidate("1", "[1, 2]", false);
  }

}
