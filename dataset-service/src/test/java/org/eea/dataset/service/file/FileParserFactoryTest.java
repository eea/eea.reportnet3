package org.eea.dataset.service.file;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

/**
 * The Class FileParserFactoryTest.
 */
public class FileParserFactoryTest {

  /** The file parser factory. */
  @InjectMocks
  private FileParserFactory fileParserFactory;


  /**
   * Inits the mocks.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Before
  public void initMocks() throws IOException {
    MockitoAnnotations.initMocks(this);
  }


  /**
   * Test create context csv.
   */
  @Test
  public void testCreateContextCsv() {
    assertNotNull(fileParserFactory.createContext("csv"));
  }

  /**
   * Test create context xml.
   */
  @Test
  public void testCreateContextXml() {
    assertNotNull(fileParserFactory.createContext("xml"));
  }

  /**
   * Test create context.
   */
  @Test
  public void testCreateContext() {
    assertNull(fileParserFactory.createContext("xx"));
  }

}
