package org.eea.dataflow.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eea.dataflow.exception.EntityNotFoundException;
import org.eea.dataflow.mapper.DataflowWebLinkMapper;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.domain.Weblink;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.dataflow.persistence.repository.WebLinkRepository;
import org.eea.dataflow.service.impl.DataflowServiceWebLinkImpl;
import org.eea.exception.EEAErrorMessage;
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
import org.springframework.dao.EmptyResultDataAccessException;

/**
 * The Class DataFlowServiceImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataFlowWebLinkServiceImplTest {

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

  @Before
  public void initMocks() {
    weblinkVO = new WeblinkVO();
    weblinkVO.setUrl("http://www.javadesdecero.es/");
    weblinkVO.setDescription("test");

    weblinkVOBad = new WeblinkVO();
    weblinkVOBad.setUrl("javadesdecero");
    weblinkVOBad.setId(1L);
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

  /** The dataflow service impl. */
  @InjectMocks
  private DataflowServiceWebLinkImpl dataflowServiceWebLinkImpl;

  /** The dataflow repository. */
  @Mock
  private DataflowRepository dataflowRepository;

  /** The web link repository. */
  @Mock
  private WebLinkRepository webLinkRepository;

  /** The dataflow web link mapper. */
  @Mock
  private DataflowWebLinkMapper dataflowWebLinkMapper;

  /** The user management controller zull. */
  @Mock
  private UserManagementController userManagementControllerZull;


  /**
   * Gets the web link.
   *
   * @return the web link
   * @throws EEAException
   */
  @Test
  public void getWebLink() throws EEAException {
    when(dataflowRepository.findDataflowByWeblinks_Id(Mockito.anyLong())).thenReturn(dataflow);
    when(userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATAFLOW,
        SecurityRoleEnum.DATA_CUSTODIAN)).thenReturn(resources);
    when(webLinkRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(new Weblink()));
    when(dataflowWebLinkMapper.entityToClass(Mockito.any())).thenReturn(new WeblinkVO());
    dataflowServiceWebLinkImpl.getWebLink(Mockito.anyLong());
    Mockito.verify(webLinkRepository, times(1)).findById(Mockito.anyLong());
  }

  /**
   * Gets the web link empty.
   *
   * @return the web link empty
   * @throws EEAException
   */
  @Test
  public void getWebLinkEmpty() throws EEAException {
    when(dataflowRepository.findDataflowByWeblinks_Id(Mockito.anyLong())).thenReturn(dataflow);
    when(userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATAFLOW,
        SecurityRoleEnum.DATA_CUSTODIAN)).thenReturn(resources);
    when(webLinkRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
    try {
      dataflowServiceWebLinkImpl.getWebLink(Mockito.anyLong());
    } catch (EntityNotFoundException e) {
      assertEquals(EEAErrorMessage.ID_LINK_INCORRECT, e.getMessage());
    }
  }

  /**
   * Save web link exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void saveWebLinkException() throws EEAException {
    when(dataflowWebLinkMapper.classToEntity(Mockito.any())).thenReturn(weblink);
    when(dataflowRepository.findById(1L)).thenReturn(Optional.empty());
    try {
      dataflowServiceWebLinkImpl.saveWebLink(1L, weblinkVOBad);
    } catch (EntityNotFoundException e) {
      assertEquals(EEAErrorMessage.DATAFLOW_NOTFOUND, e.getMessage());
    }
  }

  /**
   * Save web link.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void saveWebLink() throws EEAException {
    when(dataflowWebLinkMapper.classToEntity(Mockito.any())).thenReturn(weblink);
    when(dataflowRepository.findById(1L)).thenReturn(Optional.of(new Dataflow()));
    dataflowServiceWebLinkImpl.saveWebLink(1L, weblinkVO);
    Mockito.verify(webLinkRepository, times(1)).save(Mockito.any());
  }

  /**
   * Removes the web link exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void removeWebLinkException() throws EEAException {
    when(dataflowRepository.findDataflowByWeblinks_Id(Mockito.anyLong())).thenReturn(dataflow);
    when(userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATAFLOW,
        SecurityRoleEnum.DATA_CUSTODIAN)).thenReturn(resources);
    doThrow(EmptyResultDataAccessException.class).when(webLinkRepository).deleteById(1L);
    try {
      dataflowServiceWebLinkImpl.removeWebLink(1L);
    } catch (EntityNotFoundException e) {
      assertEquals(EEAErrorMessage.ID_LINK_INCORRECT, e.getMessage());
    }
  }

  /**
   * Removes the web link.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void removeWebLink() throws EEAException {
    when(dataflowRepository.findDataflowByWeblinks_Id(Mockito.anyLong())).thenReturn(dataflow);
    when(userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATAFLOW,
        SecurityRoleEnum.DATA_CUSTODIAN)).thenReturn(resources);
    dataflowServiceWebLinkImpl.removeWebLink(Mockito.anyLong());
    Mockito.verify(webLinkRepository, times(1)).deleteById(Mockito.any());
  }

  /**
   * Update web link exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateWebLinkException() throws EEAException {
    when(dataflowRepository.findDataflowByWeblinks_Id(Mockito.anyLong())).thenReturn(dataflow);
    when(userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATAFLOW,
        SecurityRoleEnum.DATA_CUSTODIAN)).thenReturn(resources);
    when(dataflowWebLinkMapper.classToEntity(Mockito.any())).thenReturn(weblink);
    when(webLinkRepository.findById(1L)).thenReturn(Optional.empty());
    weblinkVO.setId(1L);
    try {
      dataflowServiceWebLinkImpl.updateWebLink(weblinkVO);
    } catch (EntityNotFoundException e) {
      assertEquals(EEAErrorMessage.ID_LINK_NOT_FOUND, e.getMessage());
    }
  }

  /**
   * Update web link.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateWebLink() throws EEAException {
    when(dataflowWebLinkMapper.classToEntity(Mockito.any())).thenReturn(weblink);
    when(dataflowRepository.findDataflowByWeblinks_Id(Mockito.anyLong())).thenReturn(dataflow);
    when(userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATAFLOW,
        SecurityRoleEnum.DATA_CUSTODIAN)).thenReturn(resources);
    when(webLinkRepository.findById(1L)).thenReturn(Optional.of(new Weblink()));
    weblinkVO.setId(1L);
    dataflowServiceWebLinkImpl.updateWebLink(weblinkVO);
    Mockito.verify(webLinkRepository, times(1)).findById(1L);
  }
}
