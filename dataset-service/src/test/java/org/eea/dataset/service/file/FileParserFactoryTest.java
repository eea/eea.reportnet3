package org.eea.dataset.service.file;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.interfaces.controller.dataflow.RepresentativeController;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class FileParserFactoryTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class FileParserFactoryTest {

  /**
   * The file parser factory.
   */
  @InjectMocks
  private FileParserFactory fileParserFactory;

  @Mock
  private DatasetMetabaseService datasetMetabaseService;
  @Mock
  private RepresentativeController representativeControllerZuul;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Test create context csv.
   */
  @Test
  public void testCreateContextCsv() {
    DataSetMetabaseVO dataset = new DataSetMetabaseVO();
    dataset.setDataProviderId(1L);
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong())).thenReturn(dataset);
    DataProviderVO provider = new DataProviderVO();
    provider.setCode("Test");
    Mockito.when(representativeControllerZuul.findDataProviderById(Mockito.anyLong()))
        .thenReturn(provider);
    assertNotNull("is null", fileParserFactory.createContext("csv", 1L, ","));
  }

  /**
   * Test create context xls.
   */
  @Test
  public void testCreateContextXls() {
    DataSetMetabaseVO dataset = new DataSetMetabaseVO();
    dataset.setDataProviderId(1L);
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong())).thenReturn(dataset);
    DataProviderVO provider = new DataProviderVO();
    provider.setCode("Test");
    Mockito.when(representativeControllerZuul.findDataProviderById(Mockito.anyLong()))
        .thenReturn(provider);
    assertNotNull("is null", fileParserFactory.createContext("xls", 1L, null));
  }

  /**
   * Test create context xlsx.
   */
  @Test
  public void testCreateContextXlsx() {
    DataSetMetabaseVO dataset = new DataSetMetabaseVO();
    dataset.setDataProviderId(1L);
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong())).thenReturn(dataset);
    DataProviderVO provider = new DataProviderVO();
    provider.setCode("Test");
    Mockito.when(representativeControllerZuul.findDataProviderById(Mockito.anyLong()))
        .thenReturn(provider);
    assertNotNull("is null", fileParserFactory.createContext("xlsx", 1L, null));
  }

  /**
   * Test create context csv.
   */
  @Test
  public void testCreateContextXml() {
    DataSetMetabaseVO dataset = new DataSetMetabaseVO();
    dataset.setDataProviderId(1L);
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong())).thenReturn(dataset);
    DataProviderVO provider = new DataProviderVO();
    provider.setCode("Test");
    Mockito.when(representativeControllerZuul.findDataProviderById(Mockito.anyLong()))
        .thenReturn(provider);
    assertNull("is null", fileParserFactory.createContext("xml", 1L, null));
  }

  /**
   * Test create context.
   */
  @Test
  public void testCreateContext() {
    DataSetMetabaseVO dataset = new DataSetMetabaseVO();
    dataset.setDataProviderId(1L);
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong())).thenReturn(dataset);
    DataProviderVO provider = new DataProviderVO();
    provider.setCode("Test");
    Mockito.when(representativeControllerZuul.findDataProviderById(Mockito.anyLong()))
        .thenReturn(provider);
    assertNull("is null", fileParserFactory.createContext("xx", 1L, null));
  }

}
