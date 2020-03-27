package org.eea.rod.service.impl;

import java.util.List;
import org.eea.interfaces.vo.rod.ObligationVO;
import org.eea.rod.mapper.ObligationMapper;
import org.eea.rod.persistence.repository.ObligationFeignRepository;
import org.eea.rod.service.ObligationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The type Obligation service.
 */
@Service
public class ObligationServiceImpl implements ObligationService {

  @Autowired
  private ObligationFeignRepository obligationFeignRepository;

  @Autowired
  private ObligationMapper obligationMapper;

  @Override
  public List<ObligationVO> findOpenedObligation() {
    return obligationMapper.entityListToClass(obligationFeignRepository.findOpenedObligations());
  }

  @Override
  public ObligationVO findObligationById(Integer obligationId) {
    return obligationMapper
        .entityToClass(obligationFeignRepository.findObligationById(obligationId));
  }
}
