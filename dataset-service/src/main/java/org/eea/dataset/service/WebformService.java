package org.eea.dataset.service;

import java.util.List;
import org.eea.interfaces.vo.dataset.schemas.WebformMetabaseVO;
import org.json.simple.parser.ParseException;
import com.fasterxml.jackson.core.JsonProcessingException;



/**
 * The Interface WebformService.
 */
public interface WebformService {

  void insertWebformConfig(Long id, String name, String content) throws ParseException;

  String findWebformConfigContentById(Long id) throws JsonProcessingException;

  List<WebformMetabaseVO> getListWebforms();
}
