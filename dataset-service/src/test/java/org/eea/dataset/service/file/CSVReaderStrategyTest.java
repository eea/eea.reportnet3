package org.eea.dataset.service.file;

import java.io.InputStream;
import org.eea.dataset.exception.InvalidFileException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CSVReaderStrategyTest {

  @InjectMocks
  CSVReaderStrategy csvReaderStrategy;

  @Mock
  InputStream input;

  @Test
  public void testParseFile() throws InvalidFileException {
    csvReaderStrategy.parseFile(input, null, null);
  }

}
