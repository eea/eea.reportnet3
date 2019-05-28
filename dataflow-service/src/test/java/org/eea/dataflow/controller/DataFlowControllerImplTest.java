package org.eea.dataflow.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataFlowControllerImplTest {

  @InjectMocks
  DataFlowControllerImpl dataFlowControllerImpl;
  private DataFlowVO dataflowVO;

  @Before
  public void initMocks() {
    dataflowVO = new DataFlowVO();
    dataflowVO.setId(1L);
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testFindById() {
    assertNotNull("fail", dataFlowControllerImpl.findById(1L));
    assertEquals("fail", dataflowVO, dataFlowControllerImpl.findById(1L));
  }

  @Test
  public void testErrorHandler() {
    dataflowVO.setId(-1L);
    assertEquals("fail", dataflowVO, DataFlowControllerImpl.errorHandler(1L));
  }

}
