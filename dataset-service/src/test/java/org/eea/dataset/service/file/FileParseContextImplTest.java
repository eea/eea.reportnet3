package org.eea.dataset.service.file;

import java.io.IOException;
import java.io.InputStream;
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

@RunWith(MockitoJUnitRunner.class)
public class FileParseContextImplTest {

  @InjectMocks
  FileParseContextImpl fileParseContext;

  InputStream input;

  @Mock
  ReaderStrategy readerStrategy;


  @Before
  public void initMocks() throws IOException {
    MockitoAnnotations.initMocks(this);
  }


  @Test
  public void testParse() throws InvalidFileException {
    Mockito.when(readerStrategy.parseFile(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(new DataSetVO());
    fileParseContext.parse(Mockito.any(), Mockito.any(), Mockito.any());
  }

}
