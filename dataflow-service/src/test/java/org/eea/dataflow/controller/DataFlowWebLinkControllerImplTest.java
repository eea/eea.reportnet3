package org.eea.dataflow.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import org.eea.dataflow.service.DataflowWebLinkService;
import org.eea.exception.EEAException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.server.ResponseStatusException;

@RunWith(MockitoJUnitRunner.class)
public class DataFlowWebLinkControllerImplTest {

  @InjectMocks
  private DataFlowWebLinkControllerImpl dataFlowWebLinkControllerImpl;

  @Mock
  private DataflowWebLinkService dataflowWebLinkService;

  @Test(expected = ResponseStatusException.class)
  public void getLinkException() throws EEAException {
    doThrow(new EEAException()).when(dataflowWebLinkService).getWebLink(Mockito.anyLong());
    dataFlowWebLinkControllerImpl.getLink(Mockito.anyLong());
  }

  @Test
  public void getLink() {
    dataFlowWebLinkControllerImpl.getLink(Mockito.anyLong());
  }

  /**
   * Save link throws malformed URL exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void saveLinkThrowsMalformedURLException() throws EEAException {
    dataFlowWebLinkControllerImpl.saveLink(1L, "javadesdecero.es", "hola");
  }

  /**
   * Save link throws URI syntax exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void saveLinkThrowsURISyntaxException() throws EEAException {
    dataFlowWebLinkControllerImpl.saveLink(1L, "http://finance.yahoo.com/q/h?s=^IXIC", "hola");
  }

  /**
   * Save link throws response status exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void saveLinkThrowsResponseStatusException() throws EEAException {

    doThrow(new EEAException()).when(dataflowWebLinkService).saveWebLink(Mockito.any(),
        Mockito.any(), Mockito.any());
    dataFlowWebLinkControllerImpl.saveLink(1L, "https://www.javadesdecero.es", "hola");

  }

  /**
   * Save link.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void saveLink() throws EEAException {
    dataFlowWebLinkControllerImpl.saveLink(1L, "https://www.javadesdecero.es", "hola");
    Mockito.verify(dataflowWebLinkService, times(1)).saveWebLink(1L, "https://www.javadesdecero.es",
        "hola");
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
   * update link throws malformed URL exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void updateLinkThrowsMalformedURLException() throws EEAException {
    dataFlowWebLinkControllerImpl.updateLink(1L, "javadesdecero.es", "hola");
  }

  /**
   * update link throws URI syntax exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void updateLinkThrowsURISyntaxException() throws EEAException {
    dataFlowWebLinkControllerImpl.updateLink(1L, "http://finance.yahoo.com/q/h?s=^IXIC", "hola");
  }

  /**
   * update link throws response status exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void updateLinkThrowsResponseStatusException() throws EEAException {

    doThrow(new EEAException()).when(dataflowWebLinkService).updateWebLink(Mockito.any(),
        Mockito.any(), Mockito.any());
    dataFlowWebLinkControllerImpl.updateLink(1L, "https://www.javadesdecero.es", "hola");

  }

  /**
   * update link.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateLink() throws EEAException {
    dataFlowWebLinkControllerImpl.updateLink(1L, "https://www.javadesdecero.es", "hola");
    Mockito.verify(dataflowWebLinkService, times(1)).updateWebLink(1L, "hola",
        "https://www.javadesdecero.es");
  }
}
