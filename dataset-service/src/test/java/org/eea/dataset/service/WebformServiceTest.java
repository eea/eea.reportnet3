package org.eea.dataset.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.WebformMetabaseMapper;
import org.eea.dataset.persistence.metabase.domain.WebformMetabase;
import org.eea.dataset.persistence.metabase.repository.WebformRepository;
import org.eea.dataset.persistence.schemas.domain.webform.WebformConfig;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.persistence.schemas.repository.WebformConfigRepository;
import org.eea.dataset.service.impl.WebformServiceImpl;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.enums.WebformTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.WebformConfigVO;
import org.eea.interfaces.vo.dataset.schemas.WebformMetabaseVO;
import org.eea.interfaces.vo.dataset.schemas.WebformVO;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


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

  @Mock
  private SchemasRepository schemasRepository;

  @Mock
  private DatasetSchemaService datasetSchemaService;

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
    webformServiceImpl.insertWebformConfig("test", "{ \"prop1\" : \"param1\" }",
        WebformTypeEnum.PAMS);
    Mockito.verify(webformConfigRepository, times(1)).save(Mockito.any());
  }

  @Test(expected = EEAException.class)
  public void testInsertWebformConfigException() throws EEAException {
    try {
      webformServiceImpl.insertWebformConfig("test", "json", WebformTypeEnum.PAMS);
    } catch (EEAException e) {
      assertEquals(EEAErrorMessage.ERROR_JSON, e.getMessage());
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
    assertEquals("{\"name\":\"value1\"}",
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

    webformServiceImpl.updateWebformConfig(1L, "test", "{ \"prop1\" : \"param1\" }",
        WebformTypeEnum.PAMS);
    Mockito.verify(webformConfigRepository, times(1)).findByIdReferenced(Mockito.any());
  }

  @Test
  public void testDeleteWebformConfig() throws EEAException {
    WebformMetabase webformMetabase = new WebformMetabase();
    webformMetabase.setId(1L);
    webformMetabase.setLabel("test");
    webformMetabase.setValue("test");
    Mockito.when(webformRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(webformMetabase));
    WebformConfig webform = new WebformConfig();
    webform.setIdReferenced(1L);
    webform.setName("test");
    Map<String, Object> content = new HashMap<>();
    content.put("name", "value1");
    webform.setFile(content);
    Mockito.when(webformConfigRepository.findByIdReferenced(Mockito.anyLong())).thenReturn(webform);
    webformServiceImpl.deleteWebformConfig(1L);
    Mockito.verify(webformConfigRepository, times(1)).deleteByIdReferenced(Mockito.anyLong());
  }

  @Test
  public void testUploadWebFormConfigUpdate() throws EEAException {
    WebformConfigVO webformConfig = new WebformConfigVO();
    webformConfig.setName("test");
    webformConfig.setContent("{ \"prop1\" : \"param1\" }");
    webformConfig.setType(WebformTypeEnum.PAMS);
    webformConfig.setIdReferenced(1L);

    Long datasetId = 1L;

    List<WebformMetabaseVO> existingWebforms = new ArrayList<>();
    WebformMetabaseVO existingWebform = new WebformMetabaseVO();
    existingWebform.setLabel("test");
    existingWebform.setId(1L);
    existingWebforms.add(existingWebform);
    Mockito.when(webformServiceImpl.getListWebforms()).thenReturn(existingWebforms);

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

    Mockito.when(datasetSchemaService.getDatasetSchemaId(Mockito.anyLong())).thenReturn("schemaId");

    ResponseEntity<?> response = webformServiceImpl.uploadWebFormConfig(webformConfig, datasetId);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("", response.getBody());

    Mockito.verify(webformConfigRepository, times(1)).updateWebFormConfig(Mockito.any());
    Mockito.verify(webformRepository, times(1)).save(Mockito.any(WebformMetabase.class));
    Mockito.verify(datasetSchemaService, times(1)).updateWebform(Mockito.anyString(), Mockito.any(WebformVO.class));
  }

  @Test
  public void testUploadWebFormConfigInsert() throws EEAException {
    WebformConfigVO webformConfig = new WebformConfigVO();
    webformConfig.setName("test");
    webformConfig.setContent("{ \"prop1\" : \"param1\" }");
    webformConfig.setType(WebformTypeEnum.PAMS);

    Long datasetId = 1L;

    List<WebformMetabaseVO> existingWebforms = new ArrayList<>();
    Mockito.when(webformServiceImpl.getListWebforms()).thenReturn(existingWebforms);

    Mockito.when(datasetSchemaService.getDatasetSchemaId(Mockito.anyLong())).thenReturn("schemaId");

    ResponseEntity<?> response = webformServiceImpl.uploadWebFormConfig(webformConfig, datasetId);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("", response.getBody());

    Mockito.verify(webformConfigRepository, times(1)).save(Mockito.any(WebformConfig.class));
    Mockito.verify(webformRepository, times(1)).save(Mockito.any(WebformMetabase.class));
    Mockito.verify(datasetSchemaService, times(1)).updateWebform(Mockito.anyString(), Mockito.any(WebformVO.class));
  }
}
