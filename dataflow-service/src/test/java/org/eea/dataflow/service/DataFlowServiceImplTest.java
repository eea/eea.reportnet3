package org.eea.dataflow.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.eea.dataflow.mapper.DataflowMapper;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.dataflow.service.impl.DataflowServiceImpl;
import org.eea.exception.EEAException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RunWith(MockitoJUnitRunner.class)
public class DataFlowServiceImplTest {

  @InjectMocks
  private DataflowServiceImpl dataflowServiceImpl;

  @Mock
  private DataflowRepository dataflowRepository;

  @Mock
  private DataflowMapper dataflowMapper;

  private List<Dataflow> dataflows;

  private Pageable pageable;

  @Before
  public void initMocks() {
    dataflows = new ArrayList<>();
    dataflows.add(new Dataflow());
    pageable = PageRequest.of(1, 1);
    MockitoAnnotations.initMocks(this);
  }

  @Test(expected = EEAException.class)
  public void getByIdThrows() throws EEAException {
    dataflowServiceImpl.getById(null);
  }

  @Test
  public void getById() throws EEAException {
    when(dataflowMapper.entityToClass(Mockito.any())).thenReturn(null);
    dataflowServiceImpl.getById(1L);
    assertEquals("fail", null, dataflowServiceImpl.getById(1L));
  }


  @Test
  public void getByStatus() throws EEAException {
    when(dataflowRepository.findByStatus(Mockito.any())).thenReturn(new ArrayList<>());
    dataflowServiceImpl.getByStatus(Mockito.any());
    assertEquals("fail", new ArrayList<>(), dataflowServiceImpl.getByStatus(Mockito.any()));
  }

  @Test
  public void getPendingAccepted() throws EEAException {
    when(dataflowRepository.findPendingAccepted(Mockito.any())).thenReturn(new ArrayList<>());
    dataflowServiceImpl.getPendingAccepted(Mockito.any());
    assertEquals("fail", new ArrayList<>(), dataflowServiceImpl.getPendingAccepted(Mockito.any()));
  }

  @Test
  public void getPendingByUser() throws EEAException {
    when(dataflowRepository.findByStatusAndUserRequester(Mockito.any(), Mockito.any()))
        .thenReturn(new ArrayList<>());
    dataflowServiceImpl.getPendingByUser(Mockito.any(), Mockito.any());
    assertEquals("fail", new ArrayList<>(),
        dataflowServiceImpl.getPendingByUser(Mockito.any(), Mockito.any()));
  }

  @Test
  public void getCompletedEmpty() throws EEAException {
    when(dataflowRepository.findCompleted(Mockito.any())).thenReturn(new ArrayList<>());
    dataflowServiceImpl.getCompleted(1L, Mockito.any());
    assertEquals("fail", new ArrayList<>(), dataflowServiceImpl.getCompleted(1L, Mockito.any()));
  }

  @Test
  public void getCompleted() throws EEAException {
    when(dataflowRepository.findCompleted(Mockito.any())).thenReturn(dataflows);
    dataflowServiceImpl.getCompleted(1L, pageable);
    assertEquals("fail", new ArrayList<>(), dataflowServiceImpl.getCompleted(1L, pageable));
    dataflows.add(new Dataflow());
    dataflows.add(new Dataflow());
    when(dataflowRepository.findCompleted(Mockito.any())).thenReturn(dataflows);
    dataflowServiceImpl.getCompleted(1L, pageable);
    assertEquals("fail", new ArrayList<>(), dataflowServiceImpl.getCompleted(1L, pageable));
  }

}
