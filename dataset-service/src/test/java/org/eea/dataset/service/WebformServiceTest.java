package org.eea.dataset.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.eea.dataset.mapper.WebformMetabaseMapper;
import org.eea.dataset.persistence.metabase.repository.WebformRepository;
import org.eea.dataset.persistence.schemas.domain.webform.WebformConfig;
import org.eea.dataset.persistence.schemas.repository.WebformConfigRepository;
import org.eea.dataset.service.impl.WebformServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import com.fasterxml.jackson.core.JsonProcessingException;



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

  @Mock
  private WebformConfigRepository webformConfigRepository;

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

  @Test
  public void testInsertWebformConfig() {
    webformServiceImpl.insertWebformConfig(1L, "test", "json");
    Mockito.verify(webformConfigRepository, times(1)).save(Mockito.any());
  }

  @Test
  public void testInsertWebformConfig2() {
    webformServiceImpl.insertWebformConfig(1L, "test", "{ \"prop1\" : \"param1\" }");
    Mockito.verify(webformConfigRepository, times(1)).save(Mockito.any());
  }

  @Test
  public void testFindWebformConfigContentById() throws JsonProcessingException {
    WebformConfig webform = new WebformConfig();
    webform.setIdReferenced(1L);
    webform.setName("test");
    Map<String, Object> content = new HashMap<>();
    content.put("name", "value1");
    webform.setFile(content);
    Mockito.when(webformConfigRepository.findByIdReferenced(Mockito.anyLong())).thenReturn(webform);
    Assert.assertEquals("{\"name\":\"value1\"}",
        webformServiceImpl.findWebformConfigContentById(1L));
  }

}
