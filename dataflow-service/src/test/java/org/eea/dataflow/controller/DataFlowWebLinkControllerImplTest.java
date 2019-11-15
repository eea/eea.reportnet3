package org.eea.dataflow.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import org.eea.dataflow.mapper.DataflowWebLinkMapper;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.domain.Weblink;
import org.eea.dataflow.service.DataflowWebLinkService;
import org.eea.exception.EEAException;
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

  /** The weblink VO. */
  private WeblinkVO weblinkVO;

  /** The weblink. */
  private Weblink weblink;

  /** The weblink bad. */
  private Weblink weblinkBad;

  /** The dataflow. */
  private Dataflow dataflow;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    weblinkVO = new WeblinkVO();
    weblinkVO.setUrl("http://www.javadesdecero.es/");
    weblinkVO.setDescription("test");

    dataflow = new Dataflow();
    dataflow.setId(1L);

    weblink = new Weblink();
    weblink.setId(1L);
    weblink.setDataflow(dataflow);
    weblink.setUrl("http://www.javadesdecero.es/");
    weblink.setDescription("test");


    weblinkBad = new Weblink();
    weblinkBad.setDataflow(dataflow);
    weblinkBad.setUrl("javadesdecero");
    weblinkBad.setDescription("test");

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
    when(dataflowWebLinkMapper.classToEntity(Mockito.any())).thenReturn(weblinkBad);
    dataFlowWebLinkControllerImpl.updateLink(weblinkVO);
  }

  /**
   * update link.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateLink() throws EEAException {
    when(dataflowWebLinkMapper.classToEntity(Mockito.any())).thenReturn(weblink);
    dataFlowWebLinkControllerImpl.updateLink(weblinkVO);

    Mockito.verify(dataflowWebLinkService, times(1)).updateWebLink(weblink.getId(),
        weblink.getDescription(), weblink.getUrl());
  }
}
