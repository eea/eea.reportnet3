package org.eea.dataset.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.repository.FieldRepository;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.file.FileCommonUtils;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.utils.LiteralConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class PaMServiceImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class PaMServiceImplTest {


  /** The pa M service impl. */
  @InjectMocks
  private PaMServiceImpl paMServiceImpl;

  /** The field repository. */
  @Mock
  private FieldRepository fieldRepository;

  /** The field schemas list. */
  private List<Document> fieldSchemasList;

  /** The field value list. */
  private List<FieldValue> fieldValueList;

  /** The dataset service. */
  @Mock
  private DatasetService datasetService;

  @Mock
  private FileCommonUtils fileCommonUtils;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    fieldSchemasList = new ArrayList<>();
    Document fieldSchema = new Document();
    fieldSchema.put(LiteralConstants.PK, true);
    fieldSchema.put(LiteralConstants.ID, new ObjectId("5cf0e9b3b793310e9ceca190"));
    Document fieldSchema2 = new Document();
    fieldSchema2.put("headerName", "ListOfSinglePams");
    fieldSchema2.put(LiteralConstants.ID, new ObjectId("5cf0e9b3b793310e9ceca191"));
    fieldSchemasList.add(fieldSchema);
    fieldSchemasList.add(fieldSchema2);

    fieldValueList = new ArrayList<>();
    FieldValue fieldValue = new FieldValue();
    fieldValue.setValue("1, 2");
    fieldValueList.add(fieldValue);
  }

  /**
   * Update groups test.
   */
  @Test
  public void updateGroupsTest() {
    when(fieldRepository.findAllCascadeListOfSinglePams(Mockito.anyString(), Mockito.any()))
        .thenReturn(fieldValueList);
    paMServiceImpl.updateGroups("5cf0e9b3b793310e9ceca190", fieldValueList.get(0),
        fieldValueList.get(0));
    Mockito.verify(fieldRepository, times(1)).save(Mockito.any());
  }

  /**
   * Delete groups test.
   */
  @Test
  public void deleteGroupsTest() {
    when(fieldRepository.findAllCascadeListOfSinglePams(Mockito.anyString(), Mockito.any()))
        .thenReturn(fieldValueList);
    paMServiceImpl.deleteGroups(fieldSchemasList, "5cf0e9b3b793310e9ceca190");
    Mockito.verify(fieldRepository, times(1)).save(Mockito.any());
  }

  /**
   * Gets the simple pam list test.
   *
   * @return the simple pam list test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getSimplePamListTest() throws EEAException {
    FieldSchemaVO fieldSchema = new FieldSchemaVO();
    String idSchema = new ObjectId().toString();
    fieldSchema.setId(idSchema);
    RecordValue record = new RecordValue();
    fieldValueList = new ArrayList<>();
    FieldValue fieldValue = new FieldValue();
    fieldValue.setRecord(record);
    fieldValue.setValue("1, 2");
    fieldValue.setIdFieldSchema(idSchema);
    fieldValueList.add(fieldValue);
    record.setFields(fieldValueList);
    when(fileCommonUtils.findIdFieldSchema(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchema);
    when(fieldRepository.findFirstByIdFieldSchemaAndValue(Mockito.any(), Mockito.any()))
        .thenReturn(fieldValue);
    when(fieldRepository.findByIdFieldSchemaAndValue(Mockito.any(), Mockito.any()))
        .thenReturn(fieldValueList);
    assertNotNull(paMServiceImpl.getListSinglePaM(1L, "1"));
  }

}
