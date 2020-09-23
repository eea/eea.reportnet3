package org.eea.dataset.service.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import org.eea.dataset.service.file.interfaces.WriterStrategy;
import org.eea.exception.EEAException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class FileExportContextImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class FileExportContextImplTest {

  @InjectMocks
  private FileExportContextImpl fileExportContextImpl;

  @Mock
  private WriterStrategy writerStrategy;

  /**
   * Inits the mocks.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Test file export context impl.
   */
  @Test
  public void testFileExportContextImpl() {
    FileExportContextImpl fileExportContextImpl = new FileExportContextImpl(writerStrategy);
    assertNotNull("fail", fileExportContextImpl);
  }

  /**
   * Test file writer.
   * 
   * @throws IOException
   * @throws EEAException
   */
  @Test
  public void testFileWriter() throws IOException, EEAException {
    byte[] expectedResult = "".getBytes();
    Mockito.when(
        writerStrategy.writeFile(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean()))
        .thenReturn(expectedResult);
    assertEquals("Not equals", expectedResult, fileExportContextImpl.fileWriter(1L, 1L, "", true));
  }

}
