package org.eea.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class UtilityClassTest {

  private final String input;
  private final String expected;

  public UtilityClassTest(String input, String expected) {
    this.input = input;
    this.expected = expected;
  }

  @Parameterized.Parameters(name = "{index}: addQuotesToFieldNames({0}) => {1}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {"field", "\"field\""},
        {"field1, field2 ,field3", "\"field1\",\"field2\",\"field3\""},
        {"\"field1\",field2", "\"field2\""},
        {"", ""},
        {null, null},
        {" , , ", ""}
    });
  }

  @Test
  public void testAddQuotesToFieldNamesParameterized() {
    assertEquals(expected, UtilityClass.addQuotesToFieldNames(input));
  }

  @Test
  public void testPrivateConstructor() throws Exception {
    java.lang.reflect.Constructor<UtilityClass> constructor = UtilityClass.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    try {
      constructor.newInstance();
      fail("Expected UnsupportedOperationException to be thrown");
    } catch (Exception e) {
      assertEquals(UnsupportedOperationException.class, e.getCause().getClass());
      assertEquals("This is a utility class and cannot be instantiated", e.getCause().getMessage());
    }
  }
}
