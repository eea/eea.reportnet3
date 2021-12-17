package org.eea.dataflow.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eea.dataflow.exception.EntityNotFoundException;
import org.eea.dataflow.exception.ResourceNoFoundException;
import org.eea.dataflow.exception.WrongDataExceptions;
import org.eea.dataflow.mapper.DataflowWebLinkMapper;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.domain.Weblink;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.dataflow.persistence.repository.WebLinkRepository;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
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
  private UserManagementControllerZull userManagementControllerZull;

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

  /** The dataflow VO. */
  private DataFlowVO dataflowVO;

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
    resources = new ArrayList<>();
    resources.add(resource);

    badResource = new ResourceAccessVO();
    badResource.setId(1L);
    badResource.setResource(ResourceTypeEnum.DATAFLOW);
    badResource.setRole(SecurityRoleEnum.LEAD_REPORTER);
    badResources = new ArrayList<>();
    badResources.add(badResource);

    dataflowVO = new DataFlowVO();

    MockitoAnnotations.openMocks(this);
  }

  /**
   * Gets the web link.
   *
   * @return the web link
   * @throws EEAException the EEA exception
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
   * @throws EEAException the EEA exception
   */
  @Test(expected = EntityNotFoundException.class)
  public void getWebLinkEmpty() throws EEAException {
    when(dataflowRepository.findDataflowByWeblinks_Id(Mockito.anyLong())).thenReturn(dataflow);
    when(userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATAFLOW,
        SecurityRoleEnum.DATA_CUSTODIAN)).thenReturn(resources);
    when(webLinkRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
    try {
      dataflowServiceWebLinkImpl.getWebLink(Mockito.anyLong());
    } catch (EntityNotFoundException e) {
      assertEquals(EEAErrorMessage.ID_LINK_INCORRECT, e.getMessage());
      throw e;
    }
  }

  /**
   * Gets the web link df not found test.
   *
   * @return the web link df not found test
   * @throws EEAException the EEA exception
   */
  @Test(expected = EntityNotFoundException.class)
  public void getWebLinkDfNotFoundTest() throws EEAException {
    try {
      dataflowServiceWebLinkImpl.getWebLink(Mockito.anyLong());
    } catch (EntityNotFoundException e) {
      assertEquals("assertion error", EEAErrorMessage.DATAFLOW_NOTFOUND, e.getMessage());
      throw e;
    }
  }

  /**
   * Save web link exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EntityNotFoundException.class)
  public void saveWebLinkException() throws EEAException {
    when(dataflowWebLinkMapper.classToEntity(Mockito.any())).thenReturn(weblink);
    when(dataflowRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    try {
      dataflowServiceWebLinkImpl.saveWebLink(1L, weblinkVOBad);
    } catch (EntityNotFoundException e) {
      assertEquals(EEAErrorMessage.DATAFLOW_NOTFOUND, e.getMessage());
      throw e;
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
    when(dataflowRepository.findById(Mockito.any())).thenReturn(Optional.of(new Dataflow()));
    dataflowServiceWebLinkImpl.saveWebLink(1L, weblinkVO);
    Mockito.verify(webLinkRepository, times(1)).save(Mockito.any());
  }

  /**
   * Removes the web link exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EntityNotFoundException.class)
  public void removeWebLinkException() throws EEAException {
    when(dataflowRepository.findDataflowByWeblinks_Id(Mockito.anyLong())).thenReturn(dataflow);
    when(userManagementControllerZull.getResourcesByUser(Mockito.any(), Mockito.any()))
        .thenReturn(resources);
    doThrow(EmptyResultDataAccessException.class).when(webLinkRepository).deleteById(Mockito.any());
    try {
      dataflowServiceWebLinkImpl.removeWebLink(1L);
    } catch (EntityNotFoundException e) {
      assertEquals(EEAErrorMessage.ID_LINK_INCORRECT, e.getMessage());
      throw e;
    }
  }

  /**
   * Removes the web link exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EntityNotFoundException.class)
  public void removeWebLinkDfNotFoundTest() throws EEAException {
    try {
      dataflowServiceWebLinkImpl.removeWebLink(1L);
    } catch (EntityNotFoundException e) {
      assertEquals(EEAErrorMessage.DATAFLOW_NOTFOUND, e.getMessage());
      throw e;
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
    when(userManagementControllerZull.getResourcesByUser(Mockito.any(), Mockito.any()))
        .thenReturn(resources);
    dataflowServiceWebLinkImpl.removeWebLink(Mockito.anyLong());
    Mockito.verify(webLinkRepository, times(1)).deleteById(Mockito.any());
  }

  /**
   * Update web link exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EntityNotFoundException.class)
  public void updateWebLinkException() throws EEAException {
    when(dataflowRepository.findDataflowByWeblinks_Id(Mockito.anyLong())).thenReturn(dataflow);
    when(userManagementControllerZull.getResourcesByUser(Mockito.any(), Mockito.any()))
        .thenReturn(resources);
    when(dataflowWebLinkMapper.classToEntity(Mockito.any())).thenReturn(weblink);
    when(webLinkRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    weblinkVO.setId(1L);
    try {
      dataflowServiceWebLinkImpl.updateWebLink(weblinkVO);
    } catch (EntityNotFoundException e) {
      assertEquals(EEAErrorMessage.ID_LINK_NOT_FOUND, e.getMessage());
      throw e;
    }
  }

  /**
   * Update web link df not found test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EntityNotFoundException.class)
  public void updateWebLinkDfNotFoundTest() throws EEAException {
    when(dataflowWebLinkMapper.classToEntity(Mockito.any())).thenReturn(weblink);
    when(dataflowRepository.findDataflowByWeblinks_Id(Mockito.anyLong())).thenReturn(null);
    try {
      dataflowServiceWebLinkImpl.updateWebLink(weblinkVO);
    } catch (EntityNotFoundException e) {
      assertEquals(EEAErrorMessage.DATAFLOW_NOTFOUND, e.getMessage());
      throw e;
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
    when(userManagementControllerZull.getResourcesByUser(Mockito.any(), Mockito.any()))
        .thenReturn(resources);
    when(webLinkRepository.findById(Mockito.any())).thenReturn(Optional.of(new Weblink()));
    weblinkVO.setId(1L);
    dataflowServiceWebLinkImpl.updateWebLink(weblinkVO);
    Mockito.verify(webLinkRepository, times(1)).findById(1L);
  }


  /**
   * Gets the all weblinks by dataflow id exception test.
   *
   * @return the all weblinks by dataflow id exception test
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void getAllWeblinksByDataflowIdExceptionTest() throws EEAException {
    try {
      dataflowServiceWebLinkImpl.getAllWeblinksByDataflowId(null);
    } catch (EEAException exception) {
      assertEquals(EEAErrorMessage.DATAFLOW_NOTFOUND, exception.getMessage());
      throw exception;
    }
  }

  /**
   * Gets the all weblinks by dataflow id success test.
   *
   * @return the all weblinks by dataflow id success test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getAllWeblinksByDataflowIdSuccessTest() throws EEAException {
    List<WeblinkVO> weblinksVOExpected = new ArrayList<>();
    WeblinkVO weblinkVOaux = new WeblinkVO();
    weblinkVOaux.setId(1L);
    weblinksVOExpected.add(weblinkVO);
    dataflowVO.setWeblinks(weblinksVOExpected);
    List<Weblink> weblinks = new ArrayList<>();
    weblinks.add(weblink);
    dataflow.setWeblinks(weblinks);
    when(dataflowRepository.findById(Mockito.any())).thenReturn(Optional.of(dataflow));
    when(dataflowWebLinkMapper.entityToClass(Mockito.any())).thenReturn(weblinkVO);
    List<WeblinkVO> weblinksVO = dataflowServiceWebLinkImpl.getAllWeblinksByDataflowId(1L);
    assertEquals(weblinksVO, dataflowVO.getWeblinks());
  }

  @Test(expected = ResourceNoFoundException.class)
  public void getWeblinkResourceNoFoundExceptionTest() throws EEAException {
    try {
      Dataflow dataflow = new Dataflow();
      dataflow.setId(1L);
      ResourceAccessVO resource = new ResourceAccessVO();
      resource.setId(2L);
      List<ResourceAccessVO> resources = new ArrayList<>();
      resources.add(resource);
      when(dataflowRepository.findDataflowByWeblinks_Id(Mockito.anyLong())).thenReturn(dataflow);
      when(userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATAFLOW,
          SecurityRoleEnum.DATA_CUSTODIAN)).thenReturn(resources);
      dataflowServiceWebLinkImpl.getWebLink(Mockito.anyLong());
    } catch (ResourceNoFoundException e) {
      assertNotNull(e);
      throw e;
    }
  }

  @Test(expected = WrongDataExceptions.class)
  public void saveWebLinkWrongDataExceptionsTest() throws EEAException {
    try {
      Weblink weblink = new Weblink();
      weblink.setId(1L);
      weblink.setUrl("url");
      when(dataflowWebLinkMapper.classToEntity(Mockito.any())).thenReturn(weblink);
      dataflowServiceWebLinkImpl.saveWebLink(1L, weblinkVO);
    } catch (WrongDataExceptions e) {
      assertNotNull(e);
      throw e;
    }
  }

  @Test(expected = EEAException.class)
  public void saveWebLinkEEAExceptionTest() throws EEAException {
    try {
      when(dataflowWebLinkMapper.classToEntity(Mockito.any())).thenReturn(weblink);
      when(dataflowRepository.findById(Mockito.any())).thenReturn(Optional.of(new Dataflow()));
      when(webLinkRepository.findByUrlAndDescriptionAndDataflowId(Mockito.anyString(),
          Mockito.anyString(), Mockito.anyLong())).thenReturn(Optional.of(new Weblink()));
      dataflowServiceWebLinkImpl.saveWebLink(1L, weblinkVO);
    } catch (EEAException e) {
      assertNotNull(e);
      throw e;
    }
  }

  @Test(expected = ResourceNoFoundException.class)
  public void removeWebLinkExceptionTest() throws EEAException {
    try {
      Dataflow dataflow = new Dataflow();
      dataflow.setId(1L);
      ResourceAccessVO resource = new ResourceAccessVO();
      resource.setId(2L);
      List<ResourceAccessVO> resources = new ArrayList<>();
      resources.add(resource);
      when(dataflowRepository.findDataflowByWeblinks_Id(Mockito.anyLong())).thenReturn(dataflow);
      when(userManagementControllerZull.getResourcesByUser(Mockito.any(), Mockito.any()))
          .thenReturn(resources);
      dataflowServiceWebLinkImpl.removeWebLink(Mockito.anyLong());
    } catch (ResourceNoFoundException e) {
      assertNotNull(e);
      throw e;
    }
  }

  @Test(expected = WrongDataExceptions.class)
  public void updateWebLinkWrongDataExceptionTest() throws EEAException {
    try {
      Weblink weblink = new Weblink();
      weblink.setId(1L);
      weblink.setUrl("url");
      when(dataflowWebLinkMapper.classToEntity(Mockito.any())).thenReturn(weblink);
      when(dataflowRepository.findDataflowByWeblinks_Id(Mockito.anyLong())).thenReturn(dataflow);
      when(userManagementControllerZull.getResourcesByUser(Mockito.any(), Mockito.any()))
          .thenReturn(resources);
      weblinkVO.setId(1L);
      dataflowServiceWebLinkImpl.updateWebLink(weblinkVO);
      Mockito.verify(webLinkRepository, times(1)).findById(1L);
    } catch (WrongDataExceptions e) {
      assertNotNull(e);
      throw e;
    }
  }

  @Test(expected = ResourceNoFoundException.class)
  public void updateWeblinkResourceNoFoundExceptionTest() throws EEAException {
    try {
      Dataflow dataflow = new Dataflow();
      dataflow.setId(1L);
      ResourceAccessVO resource = new ResourceAccessVO();
      resource.setId(2L);
      List<ResourceAccessVO> resources = new ArrayList<>();
      resources.add(resource);
      when(dataflowWebLinkMapper.classToEntity(Mockito.any())).thenReturn(weblink);
      when(dataflowRepository.findDataflowByWeblinks_Id(Mockito.anyLong())).thenReturn(dataflow);
      when(userManagementControllerZull.getResourcesByUser(Mockito.any(), Mockito.any()))
          .thenReturn(resources);
      dataflowServiceWebLinkImpl.updateWebLink(weblinkVO);
    } catch (ResourceNoFoundException e) {
      assertNotNull(e);
      throw e;
    }
  }

  @Test(expected = EEAException.class)
  public void updateWeblinkEEAExceptionTest() throws EEAException {
    try {
      when(dataflowWebLinkMapper.classToEntity(Mockito.any())).thenReturn(weblink);
      when(dataflowRepository.findDataflowByWeblinks_Id(Mockito.anyLong())).thenReturn(dataflow);
      when(userManagementControllerZull.getResourcesByUser(Mockito.any(), Mockito.any()))
          .thenReturn(resources);
      when(webLinkRepository.findById(Mockito.any())).thenReturn(Optional.of(weblink));
      weblinkVO.setId(1L);
      Weblink weblink2 = new Weblink();
      weblink2.setId(3L);
      when(webLinkRepository.findByUrlAndDescriptionAndDataflowId(Mockito.anyString(),
          Mockito.anyString(), Mockito.anyLong())).thenReturn(Optional.of(weblink2));
      dataflowServiceWebLinkImpl.updateWebLink(weblinkVO);
    } catch (EEAException e) {
      assertNotNull(e);
      throw e;
    }
  }
}
