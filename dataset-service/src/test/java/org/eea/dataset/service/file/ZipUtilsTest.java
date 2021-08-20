package org.eea.dataset.service.file;

import static org.mockito.Mockito.times;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.rule.RulesSchema;
import org.eea.dataset.persistence.schemas.repository.RulesRepository;
import org.eea.dataset.persistence.schemas.repository.UniqueConstraintRepository;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.IntegrationController.IntegrationControllerZuul;
import org.eea.interfaces.controller.validation.RulesController.RulesControllerZuul;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.integration.IntegrationParams;
import org.eea.interfaces.vo.dataset.enums.FileTypeEnum;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;


/**
 * The Class ZipUtilsTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ZipUtilsTest {


  @InjectMocks
  private ZipUtils zipUtils;


  @Mock
  private DatasetService datasetService;

  @Mock
  private RulesRepository rulesRepository;

  @Mock
  private UniqueConstraintRepository uniqueConstraintRepository;


  @Mock
  private RulesControllerZuul rulesControllerZuul;

  @Mock
  private IntegrationControllerZuul integrationController;


  /**
   * Inits the mocks.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Before
  public void initMocks() throws IOException {

    MockitoAnnotations.openMocks(this);
  }


  @Test
  public void zipSchemaTest() {
    List<DataSetSchema> schemas = new ArrayList<>();
    DataSetSchema schema = new DataSetSchema();
    schema.setIdDataFlow(1L);
    schema.setIdDataSetSchema(new ObjectId());
    schemas.add(schema);
    List<DesignDataset> designs = new ArrayList<>();
    DesignDataset design = new DesignDataset();
    design.setDataSetName("test");
    design.setId(1L);
    design.setDatasetSchema(new ObjectId().toString());
    designs.add(design);

    Map<String, String> internalParameters = new HashMap<>();
    internalParameters.put(IntegrationParams.FILE_EXTENSION, FileTypeEnum.XLS.getValue());
    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.setInternalParameters(internalParameters);
    integrationVO.setOperation(IntegrationOperationTypeEnum.IMPORT);
    List<IntegrationVO> integrationVOs = new ArrayList<>();
    integrationVOs.add(integrationVO);

    Mockito.when(rulesRepository.findByIdDatasetSchema(Mockito.any()))
        .thenReturn(new RulesSchema());
    Mockito.when(uniqueConstraintRepository.findByDatasetSchemaId(Mockito.any()))
        .thenReturn(new ArrayList<>());
    Mockito.when(rulesControllerZuul.getIntegrityRulesByDatasetSchemaId(Mockito.any()))
        .thenReturn(new ArrayList<>());
    Mockito.when(integrationController.findAllIntegrationsByCriteria(Mockito.any()))
        .thenReturn(integrationVOs);

    zipUtils.zipSchema(designs, schemas, 1L);
    Mockito.verify(integrationController, times(1)).findAllIntegrationsByCriteria(Mockito.any());
  }


  /**
   * @throws IOException
   * @throws EEAException
   */
  @Test
  public void unzipSchemaTest() throws EEAException, IOException {

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    ZipOutputStream zip = new ZipOutputStream(baos);
    ZipEntry entry1 = new ZipEntry("Table.schema");
    ZipEntry entry2 = new ZipEntry("Table.qcrules");
    ZipEntry entry3 = new ZipEntry("Table.unique");
    ZipEntry entry4 = new ZipEntry("Table.names");
    ZipEntry entry5 = new ZipEntry("Table.extintegrations");
    ZipEntry entry6 = new ZipEntry("Table.integrity");
    ZipEntry entry7 = new ZipEntry("Table.ids");

    zip.putNextEntry(entry1);
    zip.putNextEntry(entry2);
    zip.putNextEntry(entry3);
    zip.putNextEntry(entry4);
    zip.putNextEntry(entry5);
    zip.putNextEntry(entry6);
    zip.putNextEntry(entry7);

    zip.close();
    MultipartFile multipartFile = new MockMultipartFile("file", "file.zip",
        "application/x-zip-compressed", baos.toByteArray());

    Assert.assertNotNull(zipUtils.unZipImportSchema(multipartFile.getInputStream(),
        multipartFile.getOriginalFilename()));
  }


}
