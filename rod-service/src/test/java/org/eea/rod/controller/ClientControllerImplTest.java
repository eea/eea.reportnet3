package org.eea.rod.controller;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import org.eea.interfaces.vo.rod.ClientVO;
import org.eea.rod.service.ClientService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ClientControllerImplTest {

  @InjectMocks
  private ClientControllerImpl clientController;
  @Mock
  private ClientService clientService;

  @Before
  public void init() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void findAll() {
    List<ClientVO> dataList = new ArrayList<>();
    ClientVO data = new ClientVO();
    data.setClientId(1);
    dataList.add(data);
    Mockito.when(clientService.findAll()).thenReturn(dataList);

    List<ClientVO> result = clientController.findAll();
    Assert.assertNotNull(result);
    Assert.assertEquals(1, result.size());
    Assert.assertEquals(1, result.get(0).getClientId().intValue());
  }
}