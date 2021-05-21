package org.eea.dataset.service.impl;

import static org.mockito.Mockito.times;
import java.util.ArrayList;
import java.util.List;
import org.eea.dataset.mapper.ReferenceDatasetMapper;
import org.eea.dataset.mapper.ReferenceDatasetPublicMapper;
import org.eea.dataset.persistence.metabase.domain.ReferenceDataset;
import org.eea.dataset.persistence.metabase.repository.ReferenceDatasetRepository;
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

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
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
    List<ReferenceDataset> references = new ArrayList<>();
    Mockito.when(referenceDatasetRepository.findByDataflowId(Mockito.anyLong()))
        .thenReturn(references);
    referenceDatasetService.getReferenceDatasetPublicByDataflow(1L);
    Mockito.verify(referenceDatasetRepository, times(1)).findByDataflowId(Mockito.any());
  }

}
