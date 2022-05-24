package org.eea.dataset.service.file;

import static org.mockito.Mockito.times;
import java.io.IOException;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.service.file.interfaces.ReaderStrategy;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;

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
    MockitoAnnotations.openMocks(this);
  }


  /**
   * Test parse.
   *
   * @throws EEAException
   * @throws IOException
   */
  @Test
  public void testParse() throws EEAException, IOException {
    MultipartFile file = Mockito.mock(MultipartFile.class);
    fileParseContext.parse(file.getInputStream(), 1L, 1L, "", 1L, "file", true, new DataSetSchema(),
        new ConnectionDataVO());
    Mockito.verify(readerStrategy, times(1)).parseFile(Mockito.any(), Mockito.anyLong(),
        Mockito.anyLong(), Mockito.any(), Mockito.anyLong(), Mockito.any(), Mockito.anyBoolean(),
        Mockito.any(), Mockito.any());
  }

}
