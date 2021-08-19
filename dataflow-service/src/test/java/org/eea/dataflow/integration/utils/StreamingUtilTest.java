package org.eea.dataflow.integration.utils;

import static org.mockito.Mockito.times;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class StreamingUtilTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class StreamingUtilTest {

  /** The streaming util. */
  @InjectMocks
  private StreamingUtil streamingUtil;

  /** The source. */
  @Mock
  private InputStream source;

  /** The target. */
  @Mock
  private OutputStream target;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Test method for
   * {@link org.eea.dataflow.integration.utils.StreamingUtil#copy(java.io.InputStream, java.io.OutputStream)}.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void testCopy() throws IOException {
    Mockito.when(source.read(Mockito.any())).thenReturn(1).thenReturn(0);
    streamingUtil.copy(source, target);
    Mockito.verify(source, times(2)).read(Mockito.any());
  }

}
