package org.eea.dataset.service.impl;

import java.util.HashMap;
import java.util.List;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.WebformMetabaseMapper;
import org.eea.dataset.persistence.metabase.domain.WebformMetabase;
import org.eea.dataset.persistence.metabase.repository.WebformRepository;
import org.eea.dataset.persistence.schemas.domain.webform.WebformConfig;
import org.eea.dataset.persistence.schemas.repository.WebformConfigRepository;
import org.eea.dataset.service.WebformService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.schemas.WebformMetabaseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The webform config repository. */
  @Autowired
  private WebformConfigRepository webformConfigRepository;


  /** The webform repository. */
  @Autowired
  WebformRepository webformRepository;


  /** The webform metabase mapper. */
  @Autowired
  private WebformMetabaseMapper webformMetabaseMapper;



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
   * @throws EEAException
   */
  @Override
  @Transactional
  public void insertWebformConfig(String name, String content) throws EEAException {

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
        webformRepository.save(webformMetabase);
        webform.setIdReferenced(webformMetabase.getId());
        webformConfigRepository.save(webform);
        LOG.info("A new webform configuration has been inserted");
      } catch (JsonProcessingException e) {
        LOG_ERROR.error("Error processing the json to insert");
        throw new EEAException(EEAErrorMessage.ERROR_JSON);
      }
    } else {
      LOG_ERROR.error("Error inserting new webform config. The name already exists");
      throw new EEAException(EEAErrorMessage.NAME_DUPLICATED);
    }
  }



  /**
   * Update webform config.
   *
   * @param id the id
   * @param name the name
   * @param content the content
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void updateWebformConfig(Long id, String name, String content) throws EEAException {

    WebformMetabase webformMetabase = webformRepository.findById(id).orElse(null);
    if (null != webformMetabase) {

      List<WebformMetabaseVO> existingWebforms = getListWebforms();
      if (existingWebforms.stream()
          .filter(w -> w.getLabel().equals(name) && !w.getId().equals(webformMetabase.getId()))
          .count() > 0) {
        LOG_ERROR.error("Error updating new webform config. The name already exists");
        throw new EEAException(EEAErrorMessage.NAME_DUPLICATED);
      }

      webformMetabase.setLabel(name);
      webformMetabase.setValue(name);

      WebformConfig webform = webformConfigRepository.findByIdReferenced(id);
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
      try {
        webform.setFile(mapper.readValue(content, HashMap.class));
        webform.setIdReferenced(id);
        webform.setName(name);

        webformConfigRepository.updateWebFormConfig(webform);
        webformRepository.save(webformMetabase);
        LOG.info("The webform configuration with id {} has been updated", id);
      } catch (JsonProcessingException e) {
        LOG_ERROR.error("Error processing the json to update");
        throw new EEAException(EEAErrorMessage.ERROR_JSON);
      }
    }
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
