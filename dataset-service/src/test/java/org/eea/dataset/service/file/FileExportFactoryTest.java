/*
 * 
 */
package org.eea.dataset.service.file;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class FileExportFactoryTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class FileExportFactoryTest {

  /** The file export factory. */
  @InjectMocks
  private FileExportFactory fileExportFactory;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Test create context.
   */
  @Test
  public void testCreateContextCsv() {
    assertNotNull("is null", fileExportFactory.createContext("csv"));
  }

  @Test
  public void testCreateContextXml() {
    assertNull("is null", fileExportFactory.createContext("xml"));
  }

  @Test
  public void testCreateContextXlsx() {
    assertNotNull("is null", fileExportFactory.createContext("xlsx"));
  }

}
