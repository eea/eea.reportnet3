package org.eea.dataset.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import java.util.ArrayList;
import java.util.Optional;
import org.eea.dataset.mapper.ReferenceDatasetMapper;
import org.eea.dataset.mapper.ReferenceDatasetPublicMapper;
import org.eea.dataset.persistence.metabase.domain.ReferenceDataset;
import org.eea.dataset.persistence.metabase.repository.ReferenceDatasetRepository;
import org.eea.dataset.persistence.schemas.domain.pkcatalogue.DataflowReferencedSchema;
import org.eea.dataset.persistence.schemas.repository.DataflowReferencedRepository;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
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
  public void updateUpdatableTest() throws EEAException {
    ReferenceDataset referenceDataset = new ReferenceDataset();
    referenceDataset.setId(1L);
    referenceDataset.setUpdatable(true);
    Mockito.when(referenceDatasetRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(referenceDataset));
    referenceDatasetService.updateUpdatable(1L, false);
    assertTrue(referenceDataset.getUpdatable().equals(Boolean.FALSE));
  }

  @Test(expected = EEAException.class)
  public void updateUpdatableExceptionTest() throws EEAException {
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
