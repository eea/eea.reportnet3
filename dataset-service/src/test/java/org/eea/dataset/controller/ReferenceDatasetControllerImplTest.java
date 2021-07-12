package org.eea.dataset.controller;

import static org.mockito.Mockito.doThrow;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eea.dataset.service.ReferenceDatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.ReferenceDatasetPublicVO;
import org.eea.interfaces.vo.dataset.ReferenceDatasetVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


/**
 * The Class ReferenceDatasetControllerImplTest.
 */
public class ReferenceDatasetControllerImplTest {


  /** The reference dataset service. */
  @Mock
  private ReferenceDatasetService referenceDatasetService;


  /** The reference dataset controller impl. */
  @InjectMocks
  private ReferenceDatasetControllerImpl referenceDatasetControllerImpl;



  @Before
  public void initMocks() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void findReferenceDatasetByDataflowIdTest() {
    List<ReferenceDatasetVO> references = new ArrayList<>();
    Mockito.when(referenceDatasetService.getReferenceDatasetByDataflowId(Mockito.anyLong()))
        .thenReturn(references);
    Assert.assertEquals(references,
        referenceDatasetControllerImpl.findReferenceDatasetByDataflowId(1L));
  }


  @Test
  public void findReferenceDataSetPublicByDataflowIdTest() {
    List<ReferenceDatasetPublicVO> references = new ArrayList<>();
    Mockito.when(referenceDatasetService.getReferenceDatasetPublicByDataflow(Mockito.anyLong()))
        .thenReturn(references);
    Assert.assertEquals(references,
        referenceDatasetControllerImpl.findReferenceDataSetPublicByDataflowId(1L));
  }

  @Test
  public void findDataflowsReferencedByDataflowIdTest() {
    Set<DataFlowVO> dataflows = new HashSet<>();
    Assert.assertEquals(dataflows,
        referenceDatasetControllerImpl.findDataflowsReferencedByDataflowId(1L));
  }

  @Test
  public void updateReferenceDatasetSuccessTest() throws EEAException {
    referenceDatasetControllerImpl.updateReferenceDataset(1L, true);
    Mockito.verify(referenceDatasetService, Mockito.times(1)).updateUpdatable(Mockito.anyLong(),
        Mockito.anyBoolean());
  }

  /**
   * Update reference dataset exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void updateReferenceDatasetExceptionTest() throws EEAException {
    doThrow(new EEAException("Fail")).when(referenceDatasetService)
        .updateUpdatable(Mockito.anyLong(), Mockito.anyBoolean());
    try {
      referenceDatasetControllerImpl.updateReferenceDataset(1L, true);
    } catch (ResponseStatusException e) {
      Assert.assertEquals("Fail", e.getReason());
      Assert.assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      throw e;
    }

  }
}
