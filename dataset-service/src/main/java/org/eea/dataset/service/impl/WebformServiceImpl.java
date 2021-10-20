package org.eea.dataset.service.impl;

import java.util.HashMap;
import java.util.List;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.WebformMetabaseMapper;
import org.eea.dataset.persistence.metabase.repository.WebformRepository;
import org.eea.dataset.persistence.schemas.domain.webform.WebformConfig;
import org.eea.dataset.persistence.schemas.repository.WebformConfigRepository;
import org.eea.dataset.service.WebformService;
import org.eea.interfaces.vo.dataset.schemas.WebformMetabaseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;



/**
 * The Class WebformServiceImpl.
 */
@Service
public class WebformServiceImpl implements WebformService {


  /** The webform config repository. */
  @Autowired
  private WebformConfigRepository webformConfigRepository;

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

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
   * @param id the id
   * @param name the name
   * @param content the content
   */
  @Override
  public void insertWebformConfig(Long id, String name, String content) {
    WebformConfig webform = new WebformConfig();

    webform.setId(new ObjectId());
    webform.setName(name);

    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
    try {
      webform.setFile(mapper.readValue(content, HashMap.class));
    } catch (JsonProcessingException e) {
      LOG_ERROR.error("Error processing the json to insert");
    }
    webform.setIdReferenced(id);

    webformConfigRepository.save(webform);
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
