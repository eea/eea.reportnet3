package org.eea.dataset.service.impl;

import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.WebformMetabaseMapper;
import org.eea.dataset.persistence.metabase.domain.WebformMetabase;
import org.eea.dataset.persistence.metabase.repository.WebformRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.webform.Webform;
import org.eea.dataset.persistence.schemas.domain.webform.WebformConfig;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.persistence.schemas.repository.WebformConfigRepository;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.WebformService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.enums.WebformTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.WebformConfigVO;
import org.eea.interfaces.vo.dataset.schemas.WebformMetabaseVO;
import org.eea.interfaces.vo.dataset.schemas.WebformVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;



/**
 * The Class WebformServiceImpl.
 */
@Service
public class WebformServiceImpl implements WebformService {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(WebformServiceImpl.class);

  /** The webform config repository. */
  private final WebformConfigRepository webformConfigRepository;


  /** The webform repository. */
  private final WebformRepository webformRepository;


  /** The webform metabase mapper. */
  private final WebformMetabaseMapper webformMetabaseMapper;


  /** The schemas repository. */
  private final SchemasRepository schemasRepository;

  /**
   * The dataset schema service
   */
  private final DatasetSchemaService datasetSchemaService;

  public WebformServiceImpl (WebformConfigRepository webformConfigRepository,  WebformRepository webformRepository, WebformMetabaseMapper webformMetabaseMapper, SchemasRepository schemasRepository, DatasetSchemaService datasetSchemaService) {
    this.webformConfigRepository = webformConfigRepository;
    this.webformRepository = webformRepository;
    this.webformMetabaseMapper = webformMetabaseMapper;
    this.schemasRepository =schemasRepository;
    this.datasetSchemaService = datasetSchemaService;
  }

  /**
   * Gets the webforms list.
   *
   * @return the webforms list
   */
  @Override
  public List<WebformMetabaseVO> getListWebforms() {
    return webformMetabaseMapper.entityListToClass(webformRepository.findAll());
  }


  /**
   * Insert webform config.
   *
   * @param name the name
   * @param content the content
   * @param type the type
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void insertWebformConfig(String name, String content, WebformTypeEnum type)
      throws EEAException {

    List<WebformMetabaseVO> existingWebforms = getListWebforms();
    Boolean nameRepeated = existingWebforms.stream().anyMatch(w -> w.getLabel().equals(name));
    if (Boolean.FALSE.equals(nameRepeated)) {

      WebformConfig webform = new WebformConfig();
      webform.setId(new ObjectId());
      webform.setName(name);

      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
      try {
        webform.setFile(mapper.readValue(content, HashMap.class));
        WebformMetabase webformMetabase = new WebformMetabase();
        webformMetabase.setLabel(name);
        webformMetabase.setValue(name);
        webformMetabase.setType(type);
        webformRepository.save(webformMetabase);
        webform.setIdReferenced(webformMetabase.getId());
        webformConfigRepository.save(webform);
        LOG.info("A new webform configuration has been inserted with name {}", name);
      } catch (JsonProcessingException e) {
        LOG.error("Error processing the json to insert webform configuration with name {}", name);
        throw new EEAException(EEAErrorMessage.ERROR_JSON);
      }
    } else {
      LOG.error("Error inserting new webform config. The name {} already exists", name);
      throw new EEAException(EEAErrorMessage.NAME_DUPLICATED);
    }
  }



  /**
   * Update webform config.
   *
   * @param id the id
   * @param name the name
   * @param content the content
   * @param type the type
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void updateWebformConfig(Long id, String name, String content, WebformTypeEnum type)
      throws EEAException {

    WebformMetabase webformMetabase = webformRepository.findById(id).orElse(null);
    if (null != webformMetabase) {

      List<WebformMetabaseVO> existingWebforms = getListWebforms();
      if (StringUtils.isNotBlank(name) && existingWebforms.stream()
          .filter(w -> w.getLabel().equals(name) && !w.getId().equals(webformMetabase.getId()))
          .count() > 0) {
        LOG.error("Error updating new webform config. The name {} already exists", name);
        throw new EEAException(EEAErrorMessage.NAME_DUPLICATED);
      }

      if (StringUtils.isNotBlank(name)) {
        webformMetabase.setLabel(name);
        webformMetabase.setValue(name);
      }

      if (type != null) {
        webformMetabase.setType(type);
      }

      WebformConfig webform = webformConfigRepository.findByIdReferenced(id);
      String previousName = webform.getName();
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
      try {
        if (StringUtils.isNotBlank(content)) {
          webform.setFile(mapper.readValue(content, HashMap.class));
        }
        webform.setIdReferenced(id);
        if (StringUtils.isNotBlank(name)) {
          webform.setName(name);
        }

        webformConfigRepository.updateWebFormConfig(webform);
        webformRepository.save(webformMetabase);
        // we need to update the webform name in all dataset schemas if has been changed
        if (StringUtils.isNotBlank(name) || null != type) {

          List<DataSetSchema> schemas = schemasRepository.findByWebformName(previousName);
          schemas.stream().forEach(s -> {
            Webform webformToChange = s.getWebform();
            if (StringUtils.isNotBlank(name)) {
              webformToChange.setName(name);
            }
            if (type != null) {
              webformToChange.setType(type.toString());
            }
            schemasRepository.updateDatasetSchemaWebForm(s.getIdDataSetSchema().toString(),
                webformToChange);
          });
          LOG.info("The webform configuration with id {} has been updated", id);
        }
      } catch (JsonProcessingException e) {
        LOG.error("Error processing the json to update webform config with name {}", name);
        throw new EEAException(EEAErrorMessage.ERROR_JSON);
      }
    }
  }

  /**
   * Delete webform config.
   *
   * @param id the id
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void deleteWebformConfig(Long id) throws EEAException {

    WebformMetabase webformMetabase = webformRepository.findById(id).orElse(null);

    if (null != webformMetabase) {
      boolean used = schemasRepository.existsWebformName(webformMetabase.getLabel());
      if (Boolean.FALSE.equals(used)) {
        WebformConfig webform = webformConfigRepository.findByIdReferenced(id);
        webformConfigRepository.deleteByIdReferenced(webform.getIdReferenced());
        webformRepository.delete(webformMetabase);
        LOG.info("The webform with id {} and name {} has been deleted", id,
            webformMetabase.getLabel());
      } else {
        LOG.error("The webform with id {} and name {} cannot be deleted because is in use",
            id, webformMetabase.getLabel());
        throw new EEAException(EEAErrorMessage.ERROR_WEBFORM_IN_USE);
      }
    }
  }

  /**
   * Upload a webform config
   *
   * @param webformConfig The webform to upload
   * @param datasetId GThe selected datasetId
   * @return The response entity
   */
  @Override
  @Transactional
  public ResponseEntity<?> uploadWebFormConfig(WebformConfigVO webformConfig, Long datasetId) {
    String message = "";
    HttpStatus status = HttpStatus.OK;

    try {
      String webformConfigName = webformConfig.getName();
      List<WebformMetabaseVO> existingWebforms = getListWebforms();
      var nameRepeated = existingWebforms.stream().filter(w -> w.getLabel().equals(webformConfigName)).findFirst();
      if (nameRepeated.isEmpty()) {
        insertWebformConfig(webformConfig.getName(), webformConfig.getContent(),
            webformConfig.getType());
      }
    } catch (EEAException e) {
      message = e.getMessage();
      status = HttpStatus.BAD_REQUEST;
      LOG.error("Error when inserting webform config {} with type {}. Message: {}", webformConfig.getName(), webformConfig.getType(), e.getMessage());
    } catch (Exception e) {
      message = e.getMessage();
      status = HttpStatus.INTERNAL_SERVER_ERROR;
      LOG.error("Unexpected error! Error inserting webform config with name {} Message: {}", webformConfig.getName(), e.getMessage());
    }

    String datasetSchemaId = null;
    try{
      datasetSchemaId = datasetSchemaService.getDatasetSchemaId(datasetId);
      WebformVO webformVO = new WebformVO();
      webformVO.setName(webformConfig.getName());
      webformVO.setType(webformConfig.getType().getValue());
      datasetSchemaService.updateWebform(datasetSchemaId, webformVO);
    } catch (Exception e) {
      message = e.getMessage();
      status = HttpStatus.INTERNAL_SERVER_ERROR;
      LOG.error("Unexpected error! Error updating dataset schema {} for datasetId {} Message: {}", datasetSchemaId, datasetId, e.getMessage());
    }
    return new ResponseEntity<>(message, status);
  }

  /**
   * Find webform config content by id.
   *
   * @param id the id
   * @return the string
   * @throws JsonProcessingException the json processing exception
   */
  @Override
  public String findWebformConfigContentById(Long id) throws JsonProcessingException {
    WebformConfig webform = webformConfigRepository.findByIdReferenced(id);
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return mapper.writeValueAsString(webform.getFile());
  }

}
