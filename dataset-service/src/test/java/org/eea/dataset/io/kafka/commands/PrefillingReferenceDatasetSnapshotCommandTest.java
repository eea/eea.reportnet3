package org.eea.dataset.io.kafka.commands;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.ReferenceDatasetRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.helper.FileTreatmentHelper;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZuul;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class PrefillingReferenceDatasetSnapshotCommandTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class PrefillingReferenceDatasetSnapshotCommandTest {



  @InjectMocks
  private PrefillingReferenceDatasetSnapshotCommand prefillingReferenceDatasetSnapshotCommand;


  @Mock
  private DataSetMetabaseRepository datasetMetabaseRepository;


  @Mock
  private ReferenceDatasetRepository referenceDatasetRepository;

  /** The file treatment helper. */
  @Mock
  private FileTreatmentHelper fileTreatmentHelper;

  @Mock
  private RecordStoreControllerZuul recordStoreControllerZuul;

  @Mock
  private SchemasRepository datasetSchemaRepository;

  /** The eea event VO. */
  private EEAEventVO eeaEventVO;

  /** The data. */
  private Map<String, Object> data;

  @Before
  public void initMocks() {
    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.COPY_REFERENCE_DATASET_SNAPSHOT_COMPLETED_EVENT);
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testExecute() throws EEAException, IOException {

    data = new HashMap<>();
    data.put("dataset_id", 1L);
    data.put("snapshot_id", 1L);
    data.put("user", "user1");
    eeaEventVO.setData(data);
    DataSetMetabase datasetMetabase = new DataSetMetabase();
    datasetMetabase.setId(1L);
    datasetMetabase.setDatasetSchema(new ObjectId().toString());

    when(datasetMetabaseRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(datasetMetabase));

    when(datasetSchemaRepository.findByIdDataSetSchema(Mockito.any()))
        .thenReturn(new DataSetSchema());

    prefillingReferenceDatasetSnapshotCommand.execute(eeaEventVO);
    Mockito.verify(fileTreatmentHelper, times(1)).createReferenceDatasetFiles(Mockito.any());

  }


  @Test
  public void getEventTypeTest() {
    assertEquals(EventType.RESTORE_PREFILLING_REFERENCE_SNAPSHOT_COMPLETED_EVENT,
        prefillingReferenceDatasetSnapshotCommand.getEventType());
  }
}
