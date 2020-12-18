package org.eea.dataset.service.impl;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.repository.FieldRepository;
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

  private List<Document> fieldSchemasList;

  private List<FieldValue> fieldValueList;

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
}
