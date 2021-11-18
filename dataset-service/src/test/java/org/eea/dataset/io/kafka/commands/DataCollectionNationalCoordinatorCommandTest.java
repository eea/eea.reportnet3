package org.eea.dataset.io.kafka.commands;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
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


/**
 * The Class DataCollectionNationalCoordinatorCommandTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataCollectionNationalCoordinatorCommandTest {

  /** The restore data collection snapshot command. */
  @InjectMocks
  private DataCollectionNationalCoordinatorCommand dataCollectionNationalCoordinatorCommand;

  /** The user management controller zull. */
  @Mock
  private UserManagementControllerZull userManagementControllerZull;

  /** The representative controller zuul. */
  @Mock
  private RepresentativeControllerZuul representativeControllerZuul;

  /** The resource management controller zull. */
  @Mock
  private ResourceManagementControllerZull resourceManagementControllerZull;

  /** The data set metabase repository. */
  @Mock
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /** The kafka sender utils. */
  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  /** The dataflow controller zuul. */
  @Mock
  private DataFlowControllerZuul dataflowControllerZuul;

  /** The eea event VO. */
  private EEAEventVO eeaEventVO;

  /** The data. */
  private Map<String, Object> data;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.DATACOLLECTION_NATIONAL_COORDINATOR_EVENT);
    SecurityContextHolder.clearContext();
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken("user", "password"));
    MockitoAnnotations.openMocks(this);
  }


  /**
   * Gets the event type test.
   *
   * @return the event type test
   */
  @Test
  public void getEventTypeTest() {
    Assert.assertEquals(EventType.DATACOLLECTION_NATIONAL_COORDINATOR_EVENT,
        dataCollectionNationalCoordinatorCommand.getEventType());
  }

  /**
   * Test execute.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testExecute() throws EEAException {
    data = new HashMap<>();
    data.put("dataflowId", 1L);
    data.put("isCreation", "true");
    eeaEventVO.setData(data);
    List<RepresentativeVO> representativesList = new ArrayList<>();
    RepresentativeVO representativeVO = new RepresentativeVO();
    representativeVO.setId(1L);
    representativeVO.setDataProviderId(1L);
    representativesList.add(representativeVO);
    Mockito.when(representativeControllerZuul.findRepresentativesByIdDataFlow(1L))
        .thenReturn(representativesList);

    List<DataProviderVO> dataproviderList = new ArrayList<>();
    DataProviderVO dataProviderVO = new DataProviderVO();
    dataProviderVO.setId(1L);
    dataproviderList.add(dataProviderVO);
    Mockito
        .when(representativeControllerZuul.findDataProvidersByIds(representativesList.stream()
            .map(RepresentativeVO::getDataProviderId).collect(Collectors.toList())))
        .thenReturn(dataproviderList);

    List<UserRepresentationVO> usersRepresentationList = new ArrayList();
    UserRepresentationVO userRepresentationVO = new UserRepresentationVO();
    userRepresentationVO.setEmail("reprotnet@reportnet.net");
    usersRepresentationList.add(userRepresentationVO);
    Mockito.when(userManagementControllerZull.getUsersByGroup(Mockito.anyString()))
        .thenReturn(usersRepresentationList);
    List<Long> datasetIds = new ArrayList<>();
    datasetIds.add(1L);
    Mockito.when(dataSetMetabaseRepository.getDatasetIdsByDataflowIdAndDataProviderId(Mockito.any(),
        Mockito.any())).thenReturn(datasetIds);
    doNothing().when(resourceManagementControllerZull).createResource(Mockito.any());

    dataCollectionNationalCoordinatorCommand.execute(eeaEventVO);
    Mockito.verify(userManagementControllerZull, times(1))
        .addContributorsToResources(Mockito.any());

  }

}
