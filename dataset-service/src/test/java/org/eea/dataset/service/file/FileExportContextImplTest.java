package org.eea.dataset.service.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    MockitoAnnotations.openMocks(this);
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
    Mockito.when(writerStrategy.writeFile(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.any())).thenReturn(expectedResult);
    assertEquals("Not equals", expectedResult,
        fileExportContextImpl.fileWriter(1L, 1L, "", true, false, null));
  }

  /**
   * Test file list writer.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  @Test
  public void testFileListWriter() throws IOException, EEAException {
    List<byte[]> expectedResult = new ArrayList<>();
    expectedResult.add("".getBytes());
    Mockito.when(writerStrategy.writeFileList(Mockito.any(), Mockito.any(), Mockito.anyBoolean(),
        Mockito.anyBoolean())).thenReturn(expectedResult);
    assertEquals("Not equals", expectedResult,
        fileExportContextImpl.fileListWriter(1L, 1L, true, true));
  }

}
