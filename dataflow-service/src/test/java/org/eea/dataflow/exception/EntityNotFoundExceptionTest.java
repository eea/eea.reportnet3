package org.eea.dataflow.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class EntityNotFoundExceptionTest {

  @Test
  public void testEntityNotFoundExceptionStringThrowable() {
    EntityNotFoundException exception = new EntityNotFoundException("", new Throwable());
    assertEquals("", exception.getMessage());
  }

  @Test
  public void testEntityNotFoundExceptionString() {
    EntityNotFoundException exception = new EntityNotFoundException("");
    assertEquals("", exception.getMessage());
  }

  @Test
  public void testEntityNotFoundException() {
    assertNotNull(new EntityNotFoundException());
  }

  @Test
  public void testEntityNotFoundExceptionThrowable() {
    assertNotNull(new EntityNotFoundException(new Throwable()));
  }

}
