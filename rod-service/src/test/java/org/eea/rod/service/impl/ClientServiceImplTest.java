package org.eea.rod.service.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import org.eea.interfaces.vo.rod.ClientVO;
import org.eea.rod.mapper.ClientMapper;
import org.eea.rod.persistence.domain.Client;
import org.eea.rod.persistence.repository.ClientFeignRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ClientServiceImplTest {

  @InjectMocks
  private ClientServiceImpl clientService;
  @Mock
  private ClientFeignRepository clientFeignRepository;
  @Mock
  private ClientMapper clientMapper;

  @Before
  public void init() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void findAll() {
    List<ClientVO> dataVoList = new ArrayList<>();
    ClientVO dataVO = new ClientVO();
    dataVO.setClientId(1);
    dataVoList.add(dataVO);

    List<Client> dataList = new ArrayList<>();
    Client data = new Client();
    data.setClientId(1);
    dataList.add(data);
    Mockito.when(clientFeignRepository.findAll()).thenReturn(dataList);
    Mockito.when(clientMapper.entityListToClass(Mockito.eq(dataList))).thenReturn(dataVoList);
    List<ClientVO> result = clientService.findAll();
    Assert.assertNotNull(result);
    Assert.assertEquals(1, result.size());
    Assert.assertEquals(1, result.get(0).getClientId().intValue());
  }
}