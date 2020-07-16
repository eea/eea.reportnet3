package org.eea.dataset.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class InvalidFileExceptionTest {

  @Test
  public void testInvalidFileExceptionStringThrowable() {
    InvalidFileException exception = new InvalidFileException("", new Throwable());
    assertEquals("", exception.getMessage());
  }

  @Test
  public void testInvalidFileExceptionString() {
    InvalidFileException exception = new InvalidFileException("");
    assertEquals("", exception.getMessage());
  }

  @Test
  public void testInvalidFileException() {
    assertNotNull(new InvalidFileException());
  }

  @Test
  public void testInvalidFileExceptionThrowable() {
    assertNotNull(new InvalidFileException(new Throwable()));
  }

}
