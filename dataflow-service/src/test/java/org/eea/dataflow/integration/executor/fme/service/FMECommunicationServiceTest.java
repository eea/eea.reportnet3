package org.eea.dataflow.integration.executor.fme.service;

import static org.mockito.Mockito.times;
import java.util.ArrayList;
import java.util.List;
import org.eea.dataflow.integration.executor.fme.mapper.FMECollectionMapper;
import org.eea.interfaces.vo.integration.fme.FMECollectionVO;
import org.eea.interfaces.vo.integration.fme.FMEItemVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * The Class FMECommunicationServiceTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class FMECommunicationServiceTest {

  /** The fme collection mapper. */
  @Mock
  private FMECollectionMapper fmeCollectionMapper;

  /** The rest template. */
  @Mock
  private RestTemplate restTemplate;

  /** The fme controller impl. */
  @InjectMocks
  private FMECommunicationService fmeCommunicationService;



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


  @Test
  public void testSubmitAsyncJob() throws Exception {
    Mockito
        .when(fmeCommunicationService.submitAsyncJob(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(5);

    Object myobjectA = new Object();
    // define the entity you want the exchange to return
    ResponseEntity<List<Object>> myEntity = new ResponseEntity<List<Object>>(HttpStatus.ACCEPTED);
    Mockito.when(restTemplate.exchange(Matchers.eq("/objects/get-objectA"),
        Matchers.eq(HttpMethod.POST), Matchers.<HttpEntity<List<Object>>>any(),
        Matchers.<ParameterizedTypeReference<List<Object>>>any())).thenReturn(myEntity);

    fmeCommunicationService.submitAsyncJob(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(fmeCommunicationService, times(1)).submitAsyncJob(Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  @Test
  public void testSendFile() throws Exception {
    // TODO
    throw new RuntimeException("not yet implemented");
  }

  @Test
  public void testReceiveFile() throws Exception {
    // TODO
    throw new RuntimeException("not yet implemented");
  }

  @Test
  public void testFindRepository() throws Exception {
    // TODO
    throw new RuntimeException("not yet implemented");
  }

  @Test
  public void testFindItems() throws Exception {
    // TODO
    throw new RuntimeException("not yet implemented");
  }

}
