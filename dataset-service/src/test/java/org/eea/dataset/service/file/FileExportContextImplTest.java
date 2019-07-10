package org.eea.dataset.service.file;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.dataset.service.file.interfaces.WriterStrategy;
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
    FileExportContextImpl init = new FileExportContextImpl(writerStrategy);
  }

  /**
   * Test file writer.
   * 
   * @throws IOException
   * @throws InvalidFileException
   */
  @Test
  public void testFileWriter() throws InvalidFileException, IOException {
    Mockito.when(writerStrategy.writeFile(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn("");
    assertEquals("", fileExportContextImpl.fileWriter(1L, 1L, ""));
  }

}
