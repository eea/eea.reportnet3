package org.eea.rod.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.eea.interfaces.vo.rod.CountryVO;
import org.eea.rod.mapper.CountryMapper;
import org.eea.rod.persistence.domain.Country;
import org.eea.rod.persistence.repository.CountryFeignRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CountryServiceImplTest {

  @InjectMocks
  private CountryServiceImpl countryService;
  @Mock
  private CountryFeignRepository CountryFeignRepository;
  @Mock
  private CountryMapper CountryMapper;

  @Before
  public void init() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void findAll() {
    List<CountryVO> dataVoList = new ArrayList<>();
    CountryVO dataVO = new CountryVO();
    dataVO.setSpatialId(1);
    dataVoList.add(dataVO);

    List<Country> dataList = new ArrayList<>();
    Country data = new Country();
    data.setSpatialId(1);
    dataList.add(data);
    Mockito.when(CountryFeignRepository.findAll()).thenReturn(dataList);
    Mockito.when(CountryMapper.entityListToClass(Mockito.eq(dataList))).thenReturn(dataVoList);
    List<CountryVO> result = countryService.findAll();
    Assert.assertNotNull(result);
    Assert.assertEquals(1, result.size());
    Assert.assertEquals(1, result.get(0).getSpatialId().intValue());
  }
}