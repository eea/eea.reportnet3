package org.eea.dataflow.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.Optional;
import org.eea.dataflow.mapper.DataflowWebLinkMapper;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.domain.Weblink;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.dataflow.persistence.repository.WebLinkRepository;
import org.eea.dataflow.service.impl.DataflowServiceWebLinkImpl;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.weblink.WeblinkVO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

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

  /**
   * Gets the web link.
   *
   * @return the web link
   * @throws EEAException
   */
  @Test
  public void getWebLink() throws EEAException {
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
  @Test(expected = EEAException.class)
  public void getWebLinkEmpty() throws EEAException {
    when(webLinkRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
    dataflowServiceWebLinkImpl.getWebLink(Mockito.anyLong());
  }

  /**
   * Save web link exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void saveWebLinkException() throws EEAException {
    when(dataflowRepository.findById(1L)).thenReturn(Optional.empty());
    dataflowServiceWebLinkImpl.saveWebLink(1L, "HOLA", "ADIOS");
  }

  /**
   * Save web link.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void saveWebLink() throws EEAException {
    when(dataflowRepository.findById(1L)).thenReturn(Optional.of(new Dataflow()));
    dataflowServiceWebLinkImpl.saveWebLink(1L, "HOLA", "ADIOS");
    Mockito.verify(webLinkRepository, times(1)).save(Mockito.any());
  }

  /**
   * Removes the web link exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void removeWebLinkException() throws EEAException {
    dataflowServiceWebLinkImpl.removeWebLink(Mockito.any());
  }

  /**
   * Removes the web link.
   *
   * @throws EEAException the EEA exception
   */
  @Test()
  public void removeWebLink() throws EEAException {
    when(webLinkRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(new Weblink()));
    dataflowServiceWebLinkImpl.removeWebLink(Mockito.anyLong());
    Mockito.verify(webLinkRepository, times(1)).findById(Mockito.anyLong());
  }

  /**
   * Update web link exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void updateWebLinkException() throws EEAException {
    when(webLinkRepository.findById(1L)).thenReturn(Optional.empty());
    dataflowServiceWebLinkImpl.updateWebLink(1L, "HOLA", "ADIOS");
  }

  /**
   * Update web link.
   *
   * @throws EEAException the EEA exception
   */
  @Test()
  public void updateWebLink() throws EEAException {
    when(webLinkRepository.findById(1L)).thenReturn(Optional.of(new Weblink()));
    dataflowServiceWebLinkImpl.updateWebLink(1L, "HOLA", "ADIOS");
    Mockito.verify(webLinkRepository, times(1)).findById(1L);
  }
}
