package org.eea.dataset.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import org.eea.dataset.mapper.WebformMetabaseMapper;
import org.eea.dataset.persistence.metabase.repository.WebformRepository;
import org.eea.dataset.service.impl.WebformServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;


/**
 * The Class WebformServiceTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class WebformServiceTest {


  @InjectMocks
  private WebformServiceImpl webformServiceImpl;

  @Mock
  private WebformMetabaseMapper webformMetabaseMapper;

  @Mock
  private WebformRepository webformRepository;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Gets the list webforms test.
   *
   * @return the list webforms test
   */
  @Test
  public void getListWebformsTest() {
    when(webformRepository.findAll()).thenReturn(new ArrayList<>());
    when(webformServiceImpl.getListWebforms()).thenReturn(new ArrayList<>());
    webformServiceImpl.getListWebforms();
    Mockito.verify(webformMetabaseMapper, times(1)).entityListToClass(Mockito.any());
  }

}
