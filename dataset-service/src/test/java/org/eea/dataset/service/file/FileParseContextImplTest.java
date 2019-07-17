package org.eea.dataset.service.file;

import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.dataset.service.file.interfaces.ReaderStrategy;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class FileParseContextImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class FileParseContextImplTest {

  /** The file parse context. */
  @InjectMocks
  private FileParseContextImpl fileParseContext;

  /** The reader strategy. */
  @Mock
  private ReaderStrategy readerStrategy;


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
   * Test parse.
   *
   * @throws InvalidFileException the invalid file exception
   */
  @Test
  public void testParse() throws InvalidFileException {
    Mockito
        .when(readerStrategy.parseFile(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(new DataSetVO());
    DataSetVO result =
        fileParseContext.parse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    assertNotNull("is null", result);
  }

}
