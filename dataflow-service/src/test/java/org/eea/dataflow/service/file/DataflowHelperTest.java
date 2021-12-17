package org.eea.dataflow.service.file;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.IntegrationController.IntegrationControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.controller.validation.RulesController.RulesControllerZuul;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.RecordSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.rule.enums.AutomaticRuleTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.uniqueContraintVO.UniqueConstraintVO;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.security.authorization.ObjectAccessRoleEnum;
import org.eea.security.jwt.utils.EeaUserDetails;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class DataflowHelperTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataflowHelperTest {

  @InjectMocks
  private DataflowHelper dataflowHelper;

  @Mock
  private DataflowRepository dataflowRepository;

  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  @Mock
  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  @Mock
  private DatasetSchemaControllerZuul datasetSchemaControllerZuul;

  @Mock
  private RulesControllerZuul rulesControllerZuul;

  @Mock
  private IntegrationControllerZuul integrationControllerZuul;

  @Test
  public void exportSchemaInformationTest() throws EEAException, IOException {
    UserDetails userDetails = EeaUserDetails.create("user",
        new HashSet<>(Arrays.asList(ObjectAccessRoleEnum.DATAFLOW_CUSTODIAN.getAccessRole(1L))));
    SecurityContextHolder.clearContext();
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
        userDetails, "password", userDetails.getAuthorities()));
    Long dataflowId = 1L;
    Dataflow df = new Dataflow();
    df.setId(dataflowId);
    Optional<Dataflow> dataflow = Optional.of(df);

    List<DesignDatasetVO> listDesignDatasetVO = new ArrayList<>();
    DesignDatasetVO designDatasetVO = new DesignDatasetVO();
    designDatasetVO.setDataSetName("");
    designDatasetVO.setDatasetSchema("");
    designDatasetVO.setId(1L);
    listDesignDatasetVO.add(designDatasetVO);

    DataSetSchemaVO datasetSchemaVO = new DataSetSchemaVO();
    datasetSchemaVO.setIdDataSetSchema("dsId");
    datasetSchemaVO.setTableSchemas(new ArrayList<>());
    TableSchemaVO tableSchemaVO = new TableSchemaVO();
    tableSchemaVO.setRecordSchema(new RecordSchemaVO());
    tableSchemaVO.getRecordSchema().setFieldSchema(new ArrayList<>());
    tableSchemaVO.setIdTableSchema("tId");
    tableSchemaVO.setNameTableSchema("tName");
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setId("fId");
    fieldSchemaVO.setName("fName");
    fieldSchemaVO.setType(DataType.TEXT);
    tableSchemaVO.getRecordSchema().getFieldSchema().add(fieldSchemaVO);
    fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setId("fId");
    fieldSchemaVO.setName("fName");
    fieldSchemaVO.setType(DataType.ATTACHMENT);
    fieldSchemaVO.setCodelistItems(new String[] {""});
    fieldSchemaVO.setValidExtensions(new String[] {""});
    tableSchemaVO.getRecordSchema().getFieldSchema().add(fieldSchemaVO);
    fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setId("fId");
    fieldSchemaVO.setName("fName");
    fieldSchemaVO.setType(DataType.ATTACHMENT);
    tableSchemaVO.getRecordSchema().getFieldSchema().add(fieldSchemaVO);

    datasetSchemaVO.getTableSchemas().add(tableSchemaVO);

    List<String> thenConditionList = new ArrayList<>();
    thenConditionList.add("");
    thenConditionList.add("");
    RulesSchemaVO rulesSchemaVO = new RulesSchemaVO();
    rulesSchemaVO.setRules(new ArrayList<>());
    RuleVO ruleVO = new RuleVO();
    ruleVO.setAutomaticType(AutomaticRuleTypeEnum.FIELD_TYPE);
    ruleVO.setReferenceId("tId");
    ruleVO.setType(EntityTypeEnum.FIELD);
    ruleVO.setThenCondition(thenConditionList);
    rulesSchemaVO.getRules().add(ruleVO);
    ruleVO = new RuleVO();
    ruleVO.setAutomaticType(AutomaticRuleTypeEnum.MANDATORY_TABLE);
    ruleVO.setReferenceId("tIdNO");
    ruleVO.setType(EntityTypeEnum.FIELD);
    ruleVO.setThenCondition(thenConditionList);
    rulesSchemaVO.getRules().add(ruleVO);
    ruleVO = new RuleVO();
    ruleVO.setAutomaticType(AutomaticRuleTypeEnum.MANDATORY_TABLE);
    ruleVO.setReferenceId("tId");
    ruleVO.setType(EntityTypeEnum.FIELD);
    ruleVO.setThenCondition(thenConditionList);
    rulesSchemaVO.getRules().add(ruleVO);

    List<UniqueConstraintVO> uniqueConstraintVOList = new ArrayList<>();
    UniqueConstraintVO uniqueConstraintVO = new UniqueConstraintVO();
    uniqueConstraintVO.setTableSchemaId("tsId");
    uniqueConstraintVO.setFieldSchemaIds(new ArrayList<>());
    uniqueConstraintVO.getFieldSchemaIds().add("tId");
    uniqueConstraintVO.getFieldSchemaIds().add("");
    uniqueConstraintVOList.add(uniqueConstraintVO);

    List<IntegrationVO> integrationVOList = new ArrayList<>();
    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.setOperation(IntegrationOperationTypeEnum.EXPORT);
    integrationVO.setId(1L);
    Map<String, String> internalParameters = new LinkedHashMap<String, String>();
    internalParameters.put("fileExtension", "");
    integrationVO.setInternalParameters(internalParameters);
    integrationVOList.add(integrationVO);


    Mockito.when(dataflowRepository.findById(dataflowId)).thenReturn(dataflow);

    Mockito.when(datasetMetabaseControllerZuul.findDesignDataSetIdByDataflowId(dataflowId))
        .thenReturn(listDesignDatasetVO);

    Mockito.when(datasetSchemaControllerZuul.findDataSchemaByDatasetIdPrivate(1L))
        .thenReturn(datasetSchemaVO);

    Mockito.when(datasetSchemaControllerZuul.getDatasetSchemaId(dataflowId)).thenReturn("dsId");

    Mockito.when(rulesControllerZuul.findRuleSchemaByDatasetIdPrivate("dsId", dataflowId))
        .thenReturn(rulesSchemaVO);

    Mockito.when(datasetSchemaControllerZuul.getPublicUniqueConstraints("dsId", dataflowId))
        .thenReturn(uniqueConstraintVOList);

    Mockito.when(integrationControllerZuul.findExtensionsAndOperationsPrivate(Mockito.any()))
        .thenReturn(integrationVOList);

    dataflowHelper.exportSchemaInformation(dataflowId);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void downloadSchemaInformationExceptionFileNotExistsTest()
      throws EEAException, IOException {
    try {
      dataflowHelper.downloadSchemaInformation(1L, "fileName");
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      throw e;
    }
  }

  @Test
  public void downloadPublicSchemaInformationTest() throws EEAException, IOException {
    assertNotNull(dataflowHelper.downloadPublicSchemaInformation(1L));
  }

  @After
  public void afterTests() {
    File file = new File("./dataflow-1-Schema_Information");
    try {
      FileUtils.deleteDirectory(file);
    } catch (IOException e) {

    }
  }
}
