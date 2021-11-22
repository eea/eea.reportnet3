package org.eea.dataflow.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.eea.dataflow.exception.EntityNotFoundException;
import org.eea.dataflow.exception.ResourceNoFoundException;
import org.eea.dataflow.exception.WrongDataExceptions;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class DataflowWebLinkControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataflowWebLinkControllerImplTest {

  /** The data flow web link controller impl. */
  @InjectMocks
  private DataflowWebLinkControllerImpl dataflowWebLinkControllerImpl;

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
    resources = new ArrayList<>();
    resources.add(resource);

    badResource = new ResourceAccessVO();
    badResource.setId(1L);
    badResource.setResource(ResourceTypeEnum.DATAFLOW);
    badResource.setRole(SecurityRoleEnum.LEAD_REPORTER);
    badResources = new ArrayList<>();
    badResources.add(badResource);


    MockitoAnnotations.openMocks(this);
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
  @Test
  public void getLinkException() throws EEAException {
    doThrow(new EEAException()).when(dataflowWebLinkService).getWebLink(Mockito.anyLong());
    try {
      dataflowWebLinkControllerImpl.getLink(1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
    }
  }

  /**
   * Gets the link entity exception.
   *
   * @return the link entity exception
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void getLinkEntityException() throws EEAException {
    doThrow(new EntityNotFoundException()).when(dataflowWebLinkService).getWebLink(Mockito.any());
    try {
      dataflowWebLinkControllerImpl.getLink(1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      throw e;
    }
  }

  /**
   * Gets the link resource exception.
   *
   * @return the link resource exception
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void getLinkResourceException() throws EEAException {
    doThrow(new ResourceNoFoundException()).when(dataflowWebLinkService).getWebLink(Mockito.any());
    try {
      dataflowWebLinkControllerImpl.getLink(1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
      throw e;
    }
  }



  /**
   * Gets the link.
   *
   * @return the link
   * @throws EEAException the EEA exception
   */
  @Test
  public void getLink() throws EEAException {
    dataflowWebLinkControllerImpl.getLink(Mockito.anyLong());
    Mockito.verify(dataflowWebLinkService, times(1)).getWebLink(Mockito.anyLong());
  }

  /**
   * Save link bad URL throws EEA exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void saveLinkEEAException() throws EEAException {
    doThrow(new EEAException()).when(dataflowWebLinkService).saveWebLink(Mockito.any(),
        Mockito.any());
    try {
      dataflowWebLinkControllerImpl.saveLink(dataflow.getId(), weblinkVOBad);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }


  /**
   * Save link entity exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void saveLinkEntityException() throws EEAException {
    doThrow(new EntityNotFoundException()).when(dataflowWebLinkService).saveWebLink(Mockito.any(),
        Mockito.any());
    try {
      dataflowWebLinkControllerImpl.saveLink(dataflow.getId(), weblinkVO);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      throw e;
    }
  }

  /**
   * Save link wrong data exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void saveLinkWrongDataException() throws EEAException {
    doThrow(new WrongDataExceptions()).when(dataflowWebLinkService).saveWebLink(Mockito.any(),
        Mockito.any());
    try {
      dataflowWebLinkControllerImpl.saveLink(dataflow.getId(), weblinkVO);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }


  /**
   * Save link.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void saveLink() throws EEAException {
    dataflowWebLinkControllerImpl.saveLink(dataflow.getId(), weblinkVO);
    Mockito.verify(dataflowWebLinkService, times(1)).saveWebLink(weblink.getDataflow().getId(),
        weblinkVO);
  }

  /**
   * Removes the linkthrows.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void removeLinkthrowsNotFound() throws EEAException {
    doThrow(new EntityNotFoundException()).when(dataflowWebLinkService)
        .removeWebLink(Mockito.any());
    try {
      dataflowWebLinkControllerImpl.removeLink(0L, 0L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      throw e;
    }
  }

  /**
   * Removes the linkthrows forbidden.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void removeLinkthrowsForbidden() throws EEAException {
    doThrow(new ResourceNoFoundException()).when(dataflowWebLinkService)
        .removeWebLink(Mockito.any());
    try {
      dataflowWebLinkControllerImpl.removeLink(0L, 0L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
      throw e;
    }
  }


  /**
   * Removes the linkthrows internal error.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void removeLinkthrowsInternalError() throws EEAException {
    doThrow(new EEAException()).when(dataflowWebLinkService).removeWebLink(Mockito.anyLong());
    try {
      dataflowWebLinkControllerImpl.removeLink(0L, 0L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }


  /**
   * Removes the link.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void removeLink() throws EEAException {
    dataflowWebLinkControllerImpl.removeLink(0L, 0L);
    Mockito.verify(dataflowWebLinkService, times(1)).removeWebLink(Mockito.anyLong());
  }


  /**
   * Update link throws EEA exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void updateLinkThrowsEEAException() throws EEAException {
    doThrow(EEAException.class).when(dataflowWebLinkService).updateWebLink(Mockito.any());
    try {
      dataflowWebLinkControllerImpl.updateLink(weblinkVO, Mockito.anyLong());
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }

  /**
   * Update link bad request exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void updateLinkBadRequestException() throws EEAException {
    doThrow(WrongDataExceptions.class).when(dataflowWebLinkService).updateWebLink(Mockito.any());
    try {
      dataflowWebLinkControllerImpl.updateLink(weblinkVO, Mockito.anyLong());
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  /**
   * Update link forbidden exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void updateLinkForbiddenException() throws EEAException {
    doThrow(ResourceNoFoundException.class).when(dataflowWebLinkService)
        .updateWebLink(Mockito.any());
    try {
      dataflowWebLinkControllerImpl.updateLink(weblinkVO, 1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
      throw e;
    }
  }

  /**
   * Update link not found exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void updateLinkNotFoundException() throws EEAException {
    doThrow(EntityNotFoundException.class).when(dataflowWebLinkService)
        .updateWebLink(Mockito.any());
    try {
      dataflowWebLinkControllerImpl.updateLink(weblinkVO, 1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      throw e;
    }
  }

  /**
   * update link.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateLink() throws EEAException {
    dataflowWebLinkControllerImpl.updateLink(weblinkVO, 1L);

    Mockito.verify(dataflowWebLinkService, times(1)).updateWebLink(weblinkVO);
  }

  /**
   * Gets the all weblinks by dataflow success test.
   *
   * @return the all weblinks by dataflow success test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getAllWeblinksByDataflowSuccessTest() throws EEAException {
    List<WeblinkVO> weblinksExpected = new ArrayList<>();
    WeblinkVO weblinkVO = new WeblinkVO();
    weblinkVO.setId(1L);
    weblinksExpected.add(weblinkVO);
    when(dataflowWebLinkService.getAllWeblinksByDataflowId(Mockito.anyLong()))
        .thenReturn(weblinksExpected);
    assertEquals(weblinksExpected, dataflowWebLinkControllerImpl.getAllWeblinksByDataflow(1L));
  }

}
