package org.eea.rod.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.eea.interfaces.vo.rod.ObligationVO;
import org.eea.rod.service.ObligationService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ObligationControllerImplTest {

  @InjectMocks
  private ObligationControllerImpl obligationController;
  @Mock
  private ObligationService obligationService;

  @Before
  public void init() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void findOpenedObligations() {
    ObligationVO obligationVO = new ObligationVO();
    obligationVO.setObligationId(1);
    List<ObligationVO> obligationVOs = new ArrayList<>();
    obligationVOs.add(obligationVO);
    Mockito.when(
        obligationService.findOpenedObligation(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(),
            Mockito.any(Date.class), Mockito.any(Date.class))).thenReturn(obligationVOs);
    List<ObligationVO> result = obligationController
        .findOpenedObligations(1, 1, 1, new Date().getTime(), new Date().getTime());
    Assert.assertNotNull(result);
    Assert.assertEquals(1, result.size());
    Assert.assertEquals(1, result.get(0).getObligationId().intValue());

  }

  @Test
  public void findObligationById() {
    ObligationVO obligationVO = new ObligationVO();
    obligationVO.setObligationId(1);
    Mockito.when(obligationService.findObligationById(Mockito.anyInt()))
        .thenReturn(obligationVO);
    ObligationVO result = obligationController.findObligationById(1);
    Assert.assertNotNull(result);
    Assert.assertEquals(1, result.getObligationId().intValue());
  }
}