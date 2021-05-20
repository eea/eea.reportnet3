package org.eea.dataset.controller;

import java.util.ArrayList;
import java.util.List;
import org.eea.dataset.service.ReferenceDatasetService;
import org.eea.interfaces.vo.dataset.ReferenceDatasetPublicVO;
import org.eea.interfaces.vo.dataset.ReferenceDatasetVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;


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

}
