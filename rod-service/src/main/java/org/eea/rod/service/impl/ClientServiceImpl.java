package org.eea.rod.service.impl;

import java.util.List;
import org.eea.interfaces.vo.rod.ClientVO;
import org.eea.rod.mapper.ClientMapper;
import org.eea.rod.persistence.repository.ClientFeignRepository;
import org.eea.rod.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The type Client service.
 */
@Service
public class ClientServiceImpl implements ClientService {

  @Autowired
  private ClientFeignRepository clientFeignRepository;

  @Autowired
  private ClientMapper clientMapper;

  @Override
  public List<ClientVO> findAll() {
    return clientMapper.entityListToClass(clientFeignRepository.findAll());
  }
}
