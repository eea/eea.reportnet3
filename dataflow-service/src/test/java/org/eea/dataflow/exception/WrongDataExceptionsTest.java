package org.eea.dataflow.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class WrongDataExceptionsTest {

  @Test
  public void testWrongDataExceptionsStringThrowable() {
    WrongDataExceptions exception = new WrongDataExceptions("", new Throwable());
    assertEquals("", exception.getMessage());
  }

  @Test
  public void testWrongDataExceptionsString() {
    WrongDataExceptions exception = new WrongDataExceptions("");
    assertEquals("", exception.getMessage());
  }

  @Test
  public void testWrongDataExceptions() {
    assertNotNull(new WrongDataExceptions());
  }

  @Test
  public void testWrongDataExceptionsThrowable() {
    assertNotNull(new WrongDataExceptions(new Throwable()));
  }

}
