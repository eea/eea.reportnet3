package org.eea.dataset.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.WebformMetabaseMapper;
import org.eea.dataset.persistence.metabase.domain.WebformMetabase;
import org.eea.dataset.persistence.metabase.repository.WebformRepository;
import org.eea.dataset.persistence.schemas.domain.webform.WebformConfig;
import org.eea.dataset.persistence.schemas.repository.WebformConfigRepository;
import org.eea.dataset.service.impl.WebformServiceImpl;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
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
  public void testInsertWebformConfig() throws EEAException {
    webformServiceImpl.insertWebformConfig("test", "{ \"prop1\" : \"param1\" }");
    Mockito.verify(webformConfigRepository, times(1)).save(Mockito.any());
  }

  @Test(expected = EEAException.class)
  public void testInsertWebformConfigException() throws EEAException {
    try {
      webformServiceImpl.insertWebformConfig("test", "json");
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.ERROR_JSON, e.getMessage());
      throw e;
    }
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

  @Test
  public void testUpdateWebformConfig() throws EEAException {
    WebformMetabase webformMetabase = new WebformMetabase();
    webformMetabase.setId(1L);
    webformMetabase.setLabel("test");
    webformMetabase.setValue("test");
    Mockito.when(webformRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(webformMetabase));
    WebformConfig webform = new WebformConfig();
    webform.setId(new ObjectId());
    webform.setName("test");
    webform.setIdReferenced(1L);
    Mockito.when(webformConfigRepository.findByIdReferenced(Mockito.anyLong())).thenReturn(webform);

    webformServiceImpl.updateWebformConfig(1L, "test", "{ \"prop1\" : \"param1\" }");
    Mockito.verify(webformConfigRepository, times(1)).findByIdReferenced(Mockito.any());
  }

}
