package org.eea.dataset.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import org.eea.dataset.service.WebformService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.schemas.WebformConfigVO;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.core.JsonProcessingException;



/**
 * The Class WebformControllerImplTest.
 */
public class WebformControllerImplTest {


  @InjectMocks
  private WebformControllerImpl webFormControllerImpl;

  @Mock
  private WebformService webformservice;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Tests the method to retrieve the list of webforms
   *
   * @return the list of webforms that will be checked
   */
  @Test
  public void getListWebformsTest() {
    assertNotNull(webFormControllerImpl.getListWebforms());
  }

  @Test
  public void testInsertWebformConfig() throws ParseException, EEAException {
    WebformConfigVO webform = new WebformConfigVO();
    webform.setContent("json");
    webform.setIdReferenced(1L);
    webform.setName("test");
    webFormControllerImpl.insertWebformConfig(webform);
    Mockito.verify(webformservice, times(1)).insertWebformConfig(Mockito.any(), Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void testInsertWebformConfigException() throws EEAException {
    doThrow(new EEAException()).when(webformservice).insertWebformConfig(Mockito.any(),
        Mockito.any());
    try {
      webFormControllerImpl.insertWebformConfig(new WebformConfigVO());
    } catch (ResponseStatusException e) {
      assertEquals(e.getStatus(), HttpStatus.BAD_REQUEST);
      throw e;
    }
  }

  @Test
  public void testFindWebformConfig() throws JsonProcessingException {
    Mockito.when(webformservice.findWebformConfigContentById(Mockito.anyLong()))
        .thenReturn("jsonContent");
    webFormControllerImpl.findWebformConfigById(1L);
    Assert.assertEquals("jsonContent", webformservice.findWebformConfigContentById(1L));
  }

  @Test(expected = ResponseStatusException.class)
  public void testFindWebformConfigException() {
    try {
      doThrow(JsonProcessingException.class).when(webformservice)
          .findWebformConfigContentById(Mockito.anyLong());
      webFormControllerImpl.findWebformConfigById(1L);
    } catch (JsonProcessingException e) {
      assertEquals("not found", e.getMessage());
    }
  }

  @Test
  public void testUpdateWebformConfig() throws ParseException, EEAException {
    WebformConfigVO webform = new WebformConfigVO();
    webform.setContent("json");
    webform.setIdReferenced(1L);
    webform.setName("test");
    webFormControllerImpl.updateWebformConfig(webform);
    Mockito.verify(webformservice, times(1)).updateWebformConfig(Mockito.anyLong(), Mockito.any(),
        Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void testUpdateWebformConfigException() throws EEAException {
    doThrow(new EEAException()).when(webformservice).updateWebformConfig(Mockito.anyLong(),
        Mockito.any(), Mockito.any());
    try {
      WebformConfigVO webform = new WebformConfigVO();
      webform.setContent("json");
      webform.setIdReferenced(1L);
      webform.setName("test");
      webFormControllerImpl.updateWebformConfig(webform);
    } catch (ResponseStatusException e) {
      assertEquals(e.getStatus(), HttpStatus.BAD_REQUEST);
      throw e;
    }
  }

  @Test
  public void testDeleteWebformConfig() throws EEAException {
    Mockito.doNothing().when(webformservice).deleteWebformConfig(Mockito.anyLong());
    webFormControllerImpl.deleteWebformConfig(1L);
    Mockito.verify(webformservice, times(1)).deleteWebformConfig(Mockito.anyLong());
  }

  @Test(expected = ResponseStatusException.class)
  public void testDeleteWebformConfigException() throws EEAException {
    doThrow(new EEAException()).when(webformservice).deleteWebformConfig(Mockito.anyLong());
    try {
      webFormControllerImpl.deleteWebformConfig(1L);
    } catch (ResponseStatusException e) {
      assertEquals(e.getStatus(), HttpStatus.BAD_REQUEST);
      throw e;
    }
  }

}
