package org.eea.dataflow.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class ResourceNoFoundExceptionTest {

  @Test
  public void testResourceNoFoundExceptionStringThrowable() {
    ResourceNoFoundException exception = new ResourceNoFoundException("", new Throwable());
    assertEquals("", exception.getMessage());
  }

  @Test
  public void testResourceNoFoundExceptionString() {
    ResourceNoFoundException exception = new ResourceNoFoundException("");
    assertEquals("", exception.getMessage());
  }

  @Test
  public void testResourceNoFoundException() {
    assertNotNull(new ResourceNoFoundException());
  }

  @Test
  public void testResourceNoFoundExceptionThrowable() {
    assertNotNull(new ResourceNoFoundException(new Throwable()));
  }

}
