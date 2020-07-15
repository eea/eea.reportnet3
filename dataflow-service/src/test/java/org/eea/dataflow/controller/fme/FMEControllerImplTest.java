package org.eea.dataflow.controller.fme;

import static org.mockito.Mockito.times;
import java.util.ArrayList;
import java.util.List;
import org.eea.dataflow.integration.executor.fme.service.FMECommunicationService;
import org.eea.interfaces.vo.integration.fme.FMECollectionVO;
import org.eea.interfaces.vo.integration.fme.FMEItemVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;


/**
 * The Class FMEControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class FMEControllerImplTest {

  /** The fme communication service. */
  @Mock
  private FMECommunicationService fmeCommunicationService;

  /** The fme controller impl. */
  @InjectMocks
  private FMEControllerImpl fmeControllerImpl;

  /** The collection VO. */
  FMECollectionVO collectionVO;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    collectionVO = new FMECollectionVO();
    collectionVO.setTotalCount(2);
    FMEItemVO itemVO1 = new FMEItemVO();
    FMEItemVO itemVO2 = new FMEItemVO();
    List<FMEItemVO> items = new ArrayList<>();
    items.add(itemVO1);
    items.add(itemVO2);
    collectionVO.setItems(items);
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Test find repositories.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindRepositories() throws Exception {
    Mockito.when(fmeControllerImpl.findRepositories()).thenReturn(collectionVO);
    fmeControllerImpl.findRepositories();
    Mockito.verify(fmeCommunicationService, times(1)).findRepository();
  }

  /**
   * Test find items.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindItems() throws Exception {
    Mockito.when(fmeControllerImpl.findItems(Mockito.any())).thenReturn(collectionVO);
    fmeControllerImpl.findItems(Mockito.any());
    Mockito.verify(fmeCommunicationService, times(1)).findItems(Mockito.any());
  }

}
