package org.eea.dataset.service.file;

import static org.junit.Assert.assertNull;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class XMLReaderStrategyTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class XMLReaderStrategyTest {

  /** The xml reader strategy. */
  @InjectMocks
  private XMLReaderStrategy xmlReaderStrategy;

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
   * Test parse file.
   */
  @Test
  public void testParseFile() {
    assertNull(xmlReaderStrategy.parseFile(null, null, null));
  }

}
