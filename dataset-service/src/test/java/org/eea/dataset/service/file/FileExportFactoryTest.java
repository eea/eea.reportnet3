/*
 *
 */
package org.eea.dataset.service.file;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.eea.interfaces.vo.dataset.enums.FileTypeEnum;
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
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Test create context.
   */
  @Test
  public void testCreateContextCsv() {
    assertNotNull("is null", fileExportFactory.createContext(FileTypeEnum.CSV.getValue()));
  }

  @Test
  public void testCreateContextXml() {
    assertNull("is null", fileExportFactory.createContext(FileTypeEnum.XML.getValue()));
  }

  @Test
  public void testCreateContextXlsx() {
    assertNotNull("is null", fileExportFactory.createContext(FileTypeEnum.XLSX.getValue()));
  }

}
