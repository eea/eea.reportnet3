package org.eea.dataset.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eea.dataset.service.EUDatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.EUDatasetVO;
import org.eea.security.authorization.ObjectAccessRoleEnum;
import org.eea.security.jwt.utils.EeaUserDetails;
import org.eea.thread.ThreadPropertiesManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;


/**
 * The Class EUDatasetControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class EUDatasetControllerImplTest {

  /** The eu dataset controller impl. */
  @InjectMocks
  private EUDatasetControllerImpl euDatasetControllerImpl;

  /** The eu dataset service. */
  @Mock
  private EUDatasetService euDatasetService;

  /** The list EU datasets. */
  private List<EUDatasetVO> listEUDatasets;


  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    listEUDatasets = new ArrayList<>();
    EUDatasetVO eudataset = new EUDatasetVO();
    eudataset.setId(1L);
    listEUDatasets.add(eudataset);

    ThreadPropertiesManager.setVariable("user", "user");
    Set<String> roles = new HashSet<>();
    roles.add(ObjectAccessRoleEnum.DATAFLOW_CUSTODIAN.getAccessRole(1L));
    UserDetails userDetails = EeaUserDetails.create("test", roles);
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    Map<String, String> details = new HashMap<>();
    details.put("", "");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);

    MockitoAnnotations.initMocks(this);
  }

  /**
   * Find EU dataset by dataflow id test.
   */
  @Test
  public void findEUDatasetByDataflowIdTest() {
    Mockito.when(euDatasetService.getEUDatasetByDataflowId(Mockito.any()))
        .thenReturn(listEUDatasets);
    Assert.assertEquals(listEUDatasets, euDatasetControllerImpl.findEUDatasetByDataflowId(1L));
  }

  /**
   * Populate data from data collection test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void populateDataFromDataCollectionTest() throws EEAException {
    doNothing().when(euDatasetService).populateEUDatasetWithDataCollection(Mockito.any());
    euDatasetControllerImpl.populateDataFromDataCollection(1L);
    Mockito.verify(euDatasetService, times(1)).populateEUDatasetWithDataCollection(Mockito.any());
  }

  /**
   * Populate data from data collection exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void populateDataFromDataCollectionExceptionTest() throws EEAException {
    doThrow(new EEAException("failed")).when(euDatasetService)
        .populateEUDatasetWithDataCollection(Mockito.any());
    euDatasetControllerImpl.populateDataFromDataCollection(1L);
    Mockito.verify(euDatasetService, times(1)).populateEUDatasetWithDataCollection(Mockito.any());

  }
}
