package org.eea.validation.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ValidationDroolsUtilsTest {

  // @Before
  // public void initMocks() {
  // MockitoAnnotations.openMocks(this);
  // }

  @Test
  public void testCodelistValidate() {
    assertTrue(ValidationDroolsUtils.codelistValidate("", "a;b;c", Boolean.TRUE));
  }

  @Test
  public void testMultiSelectCodelistValidate() {
    assertFalse(ValidationDroolsUtils.multiSelectCodelistValidate("a;b;c", "a;b;c"));
  }

  @Test
  public void testValidateRegExpressionMail() {
    assertTrue(ValidationDroolsUtils.validateRegExpression("a@a.com", "REG_EXP_EMAIL"));
  }

  @Test
  public void testValidateRegExpressionPhone() {
    assertTrue(ValidationDroolsUtils.validateRegExpression("666666666", "REG_EXP_PHONE"));
  }

  @Test
  public void testValidateRegExpressionURL() {
    assertTrue(ValidationDroolsUtils.validateRegExpression("www.a.com", "REG_EXP_URL"));
  }

  @Test
  public void testValidateRegExpressionDefault() {
    assertTrue(ValidationDroolsUtils.validateRegExpression("", ""));
  }

}
