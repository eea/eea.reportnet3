package org.eea.dataset.controller;

import org.eea.interfaces.controller.dataset.DatasetCodelistController;
import org.eea.interfaces.vo.dataset.CodelistVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/codelist")
public class DatasetCodelistControllerImpl implements DatasetCodelistController {

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  @Override
  public CodelistVO getById(Long codelistId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Long create(CodelistVO codelistVO) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Long update(CodelistVO codelistVO) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Long clone(Long codelistId, CodelistVO codelistVO) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void deleteDocument(Long codelistId) {
    // TODO Auto-generated method stub
  }

}
