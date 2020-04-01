package org.eea.rod.service.impl;

import java.util.List;
import org.eea.interfaces.vo.rod.CountryVO;
import org.eea.rod.mapper.CountryMapper;
import org.eea.rod.persistence.repository.CountryFeignRepository;
import org.eea.rod.service.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The type Country service.
 */
@Service
public class CountryServiceImpl implements CountryService {

  @Autowired
  private CountryFeignRepository countryFeignRepository;

  @Autowired
  private CountryMapper countryMapper;

  @Override
  public List<CountryVO> findAll() {
    return countryMapper.entityListToClass(countryFeignRepository.findAll());
  }
}
