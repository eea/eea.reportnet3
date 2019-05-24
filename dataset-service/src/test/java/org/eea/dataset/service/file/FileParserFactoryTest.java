package org.eea.dataset.service.file;

import static org.junit.Assert.assertNull;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class FileParserFactoryTest {

  @InjectMocks
  FileParserFactory fileParserFactory;


  @Before
  public void initMocks() throws IOException {
    MockitoAnnotations.initMocks(this);
  }


  @Test
  public void testCreateContextCsv() {
    fileParserFactory.createContext("csv");
  }

  @Test
  public void testCreateContextXml() {
    fileParserFactory.createContext("xml");
  }

  @Test
  public void testCreateContext() {
    assertNull(fileParserFactory.createContext("xx"));
  }

}
