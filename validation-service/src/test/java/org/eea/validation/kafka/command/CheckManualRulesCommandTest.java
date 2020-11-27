package org.eea.validation.kafka.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.eea.validation.service.RuleExpressionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@RunWith(MockitoJUnitRunner.class)
public class CheckManualRulesCommandTest {

  @InjectMocks
  private CheckManualRulesCommand CheckManualRulesCommand;

  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  @Mock
  private DatasetMetabaseController datasetMetabaseController;

  @Mock
  private RulesRepository rulesRepository;

  @Mock
  private SchemasRepository schemasRepository;

  @Mock
  private RuleExpressionService ruleExpressionService;

  /** The security context. */
  private SecurityContext securityContext;

  /** The authentication. */
  private Authentication authentication;


  @Before
  public void initMocks() {

    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);

    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testExecute() throws EEAException {

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    Map<String, Object> data = new HashMap<>();
    data.put("dataset_id", "1");
    data.put("user", "user");
    data.put("checkNoSQL", Boolean.TRUE);

    String schema = new ObjectId().toString();
    DataSetMetabaseVO dsMetabaseVO = new DataSetMetabaseVO();
    dsMetabaseVO.setDataflowId(1L);

    RulesSchema ruleSchema = new RulesSchema();
    List<Rule> rules = new ArrayList<>();
    Rule rule1 = new Rule();
    rule1.setAutomatic(Boolean.FALSE);
    rule1.setSqlSentence(null);
    rule1.setType(EntityTypeEnum.FIELD);
    rule1.setReferenceId(new ObjectId());
    rule1.setVerified(Boolean.TRUE);
    rule1.setEnabled(Boolean.FALSE);
    rules.add(rule1);
    ruleSchema.setRules(rules);

    Document fieldSchema = new Document();
    fieldSchema.put("typeData", DataType.NUMBER_INTEGER);

    List<Rule> rulesSQL = new ArrayList<>();

    EEAEventVO eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.VALIDATE_MANUAL_QC_COMMAND);
    eeaEventVO.setData(data);

    Mockito.when(datasetMetabaseController.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(schema);
    Mockito.when(datasetMetabaseController.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(dsMetabaseVO);
    Mockito.when(rulesRepository.findByIdDatasetSchema(Mockito.any())).thenReturn(ruleSchema);
    Mockito.when(rulesRepository.findSqlRules(Mockito.any())).thenReturn(rulesSQL);
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchema);

    Mockito.when(rulesRepository.getAllDisabledRules(Mockito.any())).thenReturn(ruleSchema);
    Mockito.when(rulesRepository.getAllUncheckedRules(Mockito.any())).thenReturn(ruleSchema);

    CheckManualRulesCommand.execute(eeaEventVO);
    Mockito.verify(kafkaSenderUtils, Mockito.times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

}
