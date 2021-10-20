package org.eea.dataset.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.webform.WebformConfig;
import org.eea.dataset.persistence.schemas.repository.WebformConfigRepository;
import org.eea.dataset.service.WebformService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.schemas.WebformVO;
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

  @Autowired
  private WebformConfigRepository webformConfigRepository;

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


  /**
   * Gets the list webforms by dataset id.
   *
   * @param datasetId the dataset id
   * @return the list webforms by dataset id
   * @throws EEAException the EEA exception
   */
  @Override
  public List<WebformVO> getListWebformsByDatasetId(Long datasetId) throws EEAException {

    List<WebformVO> webformsList = new ArrayList<>();

    return webformsList;
  }


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

  @Override
  public String findWebformConfigContentById(Long id) throws JsonProcessingException {
    WebformConfig webform = webformConfigRepository.findByIdReferenced(id);
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return mapper.writeValueAsString(webform.getFile());
  }

}
