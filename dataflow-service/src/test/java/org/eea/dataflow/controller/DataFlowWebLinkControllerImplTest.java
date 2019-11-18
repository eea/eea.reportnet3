package org.eea.dataflow.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.eea.dataflow.mapper.DataflowWebLinkMapper;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.domain.Weblink;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.dataflow.service.DataflowWebLinkService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.ums.UserManagementController;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.interfaces.vo.weblink.WeblinkVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class DataFlowWebLinkControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataFlowWebLinkControllerImplTest {

  /** The data flow web link controller impl. */
  @InjectMocks
  private DataFlowWebLinkControllerImpl dataFlowWebLinkControllerImpl;

  /** The dataflow web link service. */
  @Mock
  private DataflowWebLinkService dataflowWebLinkService;

  /** The dataflow repository. */
  @Mock
  private DataflowRepository dataflowRepository;

  /** The user management controller zull. */
  @Mock
  private UserManagementController userManagementControllerZull;

  /** The weblink VO. */
  private WeblinkVO weblinkVO;

  /** The weblink VO. */
  private WeblinkVO weblinkVOBad;

  /** The weblink. */
  private Weblink weblink;

  /** The weblink bad. */
  private Weblink weblinkBad;

  /** The dataflow. */
  private Dataflow dataflow;

  /** The resource. */
  private ResourceAccessVO resource;

  /** The resources. */
  private List<ResourceAccessVO> resources;

  /** The resource. */
  private ResourceAccessVO badResource;

  /** The resources. */
  private List<ResourceAccessVO> badResources;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    weblinkVO = new WeblinkVO();
    weblinkVO.setUrl("http://www.javadesdecero.es/");
    weblinkVO.setDescription("test");

    weblinkVOBad = new WeblinkVO();
    weblinkVOBad.setUrl("javadesdecero");
    weblinkVOBad.setDescription("test");

    dataflow = new Dataflow();
    dataflow.setId(1L);

    weblink = new Weblink();
    weblink.setId(1L);
    weblink.setDataflow(dataflow);
    weblink.setUrl("http://www.javadesdecero.es/");
    weblink.setDescription("test");


    weblinkBad = new Weblink();
    weblinkBad.setId(1L);
    weblinkBad.setDataflow(dataflow);
    weblinkBad.setUrl("javadesdecero");
    weblinkBad.setDescription("test");

    resource = new ResourceAccessVO();
    resource.setId(1L);
    resource.setResource(ResourceTypeEnum.DATAFLOW);
    resource.setRole(SecurityRoleEnum.DATA_CUSTODIAN);
    resources = new ArrayList<ResourceAccessVO>();
    resources.add(resource);

    badResource = new ResourceAccessVO();
    badResource.setId(1L);
    badResource.setResource(ResourceTypeEnum.DATAFLOW);
    badResource.setRole(SecurityRoleEnum.DATA_PROVIDER);
    badResources = new ArrayList<ResourceAccessVO>();
    badResources.add(badResource);


    MockitoAnnotations.initMocks(this);
  }

  /** The dataflow web link mapper. */
  @Mock
  private DataflowWebLinkMapper dataflowWebLinkMapper;

  /**
   * Gets the link exception.
   *
   * @return the link exception
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void getLinkException() throws EEAException {
    when(dataflowRepository.findDataflowByWeblinks_Id(Mockito.anyLong())).thenReturn(dataflow);
    when(userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATAFLOW))
        .thenReturn(resources);
    doThrow(new EEAException()).when(dataflowWebLinkService).getWebLink(Mockito.anyLong());
    dataFlowWebLinkControllerImpl.getLink(Mockito.anyLong());
  }



  /**
   * Gets the link.
   *
   * @return the link
   */
  @Test
  public void getLink() {
    when(dataflowRepository.findDataflowByWeblinks_Id(Mockito.anyLong())).thenReturn(dataflow);
    when(userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATAFLOW))
        .thenReturn(resources);
    dataFlowWebLinkControllerImpl.getLink(Mockito.anyLong());
  }


  /**
   * Save link throws EEA exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void saveLinkThrowsEEAException() throws EEAException {
    when(dataflowWebLinkMapper.classToEntity(Mockito.any())).thenReturn(weblink);
    doThrow(EEAException.class).when(dataflowWebLinkService).saveWebLink(Mockito.any(),
        Mockito.any(), Mockito.any());
    dataFlowWebLinkControllerImpl.saveLink(dataflow.getId(), weblinkVO);
  }


  /**
   * Save link bad URL throws EEA exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void saveLinkBadURLThrowsEEAException() throws EEAException {
    when(dataflowWebLinkMapper.classToEntity(Mockito.any())).thenReturn(weblinkBad);
    dataFlowWebLinkControllerImpl.saveLink(dataflow.getId(), weblinkVO);
  }


  /**
   * Save link.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void saveLink() throws EEAException {
    when(dataflowWebLinkMapper.classToEntity(Mockito.any())).thenReturn(weblink);
    dataFlowWebLinkControllerImpl.saveLink(dataflow.getId(), weblinkVO);
    Mockito.verify(dataflowWebLinkService, times(1)).saveWebLink(weblink.getDataflow().getId(),
        weblink.getUrl(), weblink.getDescription());
  }

  /**
   * Removes the linkthrows.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void removeLinkthrows() throws EEAException {
    when(dataflowRepository.findDataflowByWeblinks_Id(Mockito.anyLong())).thenReturn(dataflow);
    when(userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATAFLOW))
        .thenReturn(resources);
    doThrow(new EEAException()).when(dataflowWebLinkService).removeWebLink(Mockito.anyLong());
    dataFlowWebLinkControllerImpl.removeLink(Mockito.anyLong());
  }


  /**
   * Removes the link.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void removeLink() throws EEAException {
    when(dataflowRepository.findDataflowByWeblinks_Id(Mockito.anyLong())).thenReturn(dataflow);
    when(userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATAFLOW))
        .thenReturn(resources);
    dataFlowWebLinkControllerImpl.removeLink(Mockito.anyLong());
    Mockito.verify(dataflowWebLinkService, times(1)).removeWebLink(Mockito.anyLong());
  }


  /**
   * Update link throws EEA exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void updateLinkThrowsEEAException() throws EEAException {
    when(dataflowRepository.findDataflowByWeblinks_Id(Mockito.anyLong())).thenReturn(dataflow);
    when(userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATAFLOW))
        .thenReturn(resources);
    when(dataflowWebLinkMapper.classToEntity(Mockito.any())).thenReturn(weblink);
    doThrow(EEAException.class).when(dataflowWebLinkService).updateWebLink(Mockito.any(),
        Mockito.any(), Mockito.any());
    dataFlowWebLinkControllerImpl.updateLink(weblinkVO);
  }



  /**
   * Update link bad URL throws EEA exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void updateLinkBadURLThrowsEEAException() throws EEAException {
    Dataflow dataflow = new Dataflow();
    dataflow.setId(1L);
    when(dataflowWebLinkMapper.classToEntity(Mockito.any())).thenReturn(weblinkBad);
    when(dataflowRepository.findDataflowByWeblinks_Id(Mockito.anyLong())).thenReturn(dataflow);
    when(userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATAFLOW))
        .thenReturn(resources);
    dataFlowWebLinkControllerImpl.updateLink(weblinkVOBad);
  }

  /**
   * update link.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateLink() throws EEAException {
    when(dataflowWebLinkMapper.classToEntity(Mockito.any())).thenReturn(weblink);
    when(dataflowRepository.findDataflowByWeblinks_Id(Mockito.anyLong())).thenReturn(dataflow);
    when(userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATAFLOW))
        .thenReturn(resources);
    dataFlowWebLinkControllerImpl.updateLink(weblinkVO);

    Mockito.verify(dataflowWebLinkService, times(1)).updateWebLink(weblink.getId(),
        weblink.getDescription(), weblink.getUrl());
  }
}
