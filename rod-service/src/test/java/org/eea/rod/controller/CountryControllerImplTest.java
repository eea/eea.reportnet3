package org.eea.rod.controller;

import java.util.ArrayList;
import java.util.List;
import org.eea.interfaces.vo.rod.ClientVO;
import org.eea.interfaces.vo.rod.CountryVO;
import org.eea.rod.service.CountryService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CountryControllerImplTest {

  @InjectMocks
  private CountryControllerImpl countryController;
  @Mock
  private CountryService countryService;

  @Before
  public void init() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void findAll() {
    List<CountryVO> dataList = new ArrayList<>();
    CountryVO data = new CountryVO();
    data.setSpatialId(1);
    dataList.add(data);
    Mockito.when(countryService.findAll()).thenReturn(dataList);

    List<CountryVO> result = countryController.findAll();
    Assert.assertNotNull(result);
    Assert.assertEquals(1, result.size());
    Assert.assertEquals(1, result.get(0).getSpatialId().intValue());
  }
}