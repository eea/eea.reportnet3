package org.eea.dataset.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eea.dataset.mapper.ReferenceDatasetMapper;
import org.eea.dataset.mapper.ReferenceDatasetPublicMapper;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.ReferenceDataset;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.ReferenceDatasetRepository;
import org.eea.dataset.persistence.schemas.domain.pkcatalogue.DataflowReferencedSchema;
import org.eea.dataset.persistence.schemas.repository.DataflowReferencedRepository;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.vo.dataset.ReferenceDatasetVO;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;


/**
 * The Class ReferenceDatasetServiceImplTest.
 */
public class ReferenceDatasetServiceImplTest {

  /** The reference dataset service. */
  @InjectMocks
  private ReferenceDatasetServiceImpl referenceDatasetService;

  /** The reference dataset repository. */
  @Mock
  private ReferenceDatasetRepository referenceDatasetRepository;

  /** The reference dataset mapper. */
  @Mock
  private ReferenceDatasetMapper referenceDatasetMapper;

  /** The reference dataset public mapper. */
  @Mock
  private ReferenceDatasetPublicMapper referenceDatasetPublicMapper;

  @Mock
  private DataflowReferencedRepository dataflowReferencedRepository;

  @Mock
  private DataFlowControllerZuul dataflowControllerZuul;

  @Mock
  private DatasetSchemaService datasetSchemaService;

  @Mock
  private DataSetMetabaseRepository datasetMetabaseRepository;

  @Mock
  private DatasetService datasetService;

  /** The data set metabase. */
  @Mock
  private DataSetMetabase dataSetMetabase;

  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }


  @Test
  public void getReferenceDatasetByDataflowIdTest() {
    referenceDatasetService.getReferenceDatasetByDataflowId(1L);
    Mockito.verify(referenceDatasetRepository, times(1)).findByDataflowId(Mockito.any());
  }


  /**
   * Gets the reference dataset public by dataflow test.
   *
   * @return the reference dataset public by dataflow test
   */
  @Test
  public void getReferenceDatasetPublicByDataflowTest() {
    referenceDatasetService.getReferenceDatasetPublicByDataflow(1L);
    Mockito.verify(referenceDatasetRepository, times(1)).findByDataflowId(Mockito.any());

  }

  @Test
  public void getReferenceDatasetPublicByDataflowTest2222222222() {
    List<ReferenceDatasetVO> referenceDatasets = new ArrayList<>();
    ReferenceDatasetVO referenceDatasetVO = new ReferenceDatasetVO();
    referenceDatasetVO.setDatasetSchema("schemaName");
    referenceDatasets.add(referenceDatasetVO);
    referenceDatasets.add(referenceDatasetVO);
    DataSetSchemaVO datasetSchemaVOTrue = new DataSetSchemaVO();
    datasetSchemaVOTrue.setAvailableInPublic(true);
    DataSetSchemaVO datasetSchemaVOFalse = new DataSetSchemaVO();
    datasetSchemaVOFalse.setAvailableInPublic(false);

    Mockito.when(referenceDatasetService.getReferenceDatasetByDataflowId(1L))
        .thenReturn(referenceDatasets);
    Mockito.when(datasetSchemaService.getDataSchemaById("schemaName"))
        .thenReturn(datasetSchemaVOTrue).thenReturn(datasetSchemaVOFalse);
    referenceDatasetService.getReferenceDatasetPublicByDataflow(1L);
    Mockito.verify(referenceDatasetRepository, times(2)).findByDataflowId(Mockito.any());

  }

  @Test
  public void getDataflowsReferencedTest() {
    DataflowReferencedSchema dataflow = new DataflowReferencedSchema();
    dataflow.setReferencedByDataflow(new ArrayList<>());
    dataflow.getReferencedByDataflow().add(1L);
    Mockito.when(dataflowReferencedRepository.findByDataflowId(Mockito.anyLong()))
        .thenReturn(dataflow);
    referenceDatasetService.getDataflowsReferenced(1L);
    Mockito.verify(dataflowControllerZuul, times(1)).getMetabaseById(Mockito.any());
  }

  @Test
  public void getDataflowsReferencedDataflowIdNullTest() {
    DataflowReferencedSchema dataflow = new DataflowReferencedSchema();
    dataflow.setReferencedByDataflow(null);
    Mockito.when(dataflowReferencedRepository.findByDataflowId(Mockito.anyLong()))
        .thenReturn(dataflow);
    referenceDatasetService.getDataflowsReferenced(1L);
    Mockito.verify(dataflowControllerZuul, times(0)).getMetabaseById(Mockito.any());
  }

  @Test
  public void getDataflowsReferencedDataflowReferenceSchemaNullTest() {
    Mockito.when(dataflowReferencedRepository.findByDataflowId(Mockito.anyLong())).thenReturn(null);
    referenceDatasetService.getDataflowsReferenced(1L);
    Mockito.verify(dataflowControllerZuul, times(0)).getMetabaseById(Mockito.any());
  }

  @Test
  public void updateUpdatableTest() throws EEAException, IOException {
    ReferenceDataset referenceDataset = new ReferenceDataset();
    referenceDataset.setId(1L);
    referenceDataset.setUpdatable(true);
    Mockito.when(referenceDatasetRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(referenceDataset));
    Optional<DataSetMetabase> dataSetMetabase = Optional.of(new DataSetMetabase());
    Mockito.when(datasetMetabaseRepository.findById(Mockito.anyLong())).thenReturn(dataSetMetabase);
    referenceDatasetService.updateUpdatable(1L, false);
    assertTrue(referenceDataset.getUpdatable().equals(Boolean.FALSE));
  }

  @Test
  public void updateUpdatableTrueTest() throws EEAException, IOException {
    ReferenceDataset referenceDataset = new ReferenceDataset();
    referenceDataset.setId(1L);
    referenceDataset.setUpdatable(true);
    Mockito.when(referenceDatasetRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(referenceDataset));
    referenceDatasetService.updateUpdatable(1L, true);
    assertTrue(referenceDataset.getUpdatable().equals(Boolean.TRUE));
  }

  @Test(expected = EEAException.class)
  public void updateUpdatableExceptionTest() throws EEAException, IOException {
    Mockito.when(referenceDatasetRepository.findById(Mockito.any()))
        .thenReturn(Optional.ofNullable(null));
    try {
      referenceDatasetService.updateUpdatable(1L, true);
    } catch (EEAException e) {
      assertEquals(EEAErrorMessage.DATASET_NOTFOUND, e.getMessage());
      throw e;
    }
  }

}
