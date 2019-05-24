package org.eea.dataset.service.file;

import static org.junit.Assert.assertNull;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XMLReaderStrategyTest {

  @InjectMocks
  XMLReaderStrategy xmlReaderStrategy;

  @Before
  public void initMocks() throws IOException {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testParseFile() {
    assertNull(xmlReaderStrategy.parseFile(null, null, null));
  }

}
