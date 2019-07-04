package org.eea.dataset.controller;

import java.util.List;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/datasetmetabase")
public class DataSetMetabaseControllerImpl implements DatasetMetabaseController {


  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

  @Override
  @RequestMapping(value = "/dataflow/{id}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<DataSetVO> findDataSetIdByDataflowId(Long idDataflow) {

    return datasetMetabaseService.getDataSetIdByDataflowId(idDataflow);

  }

}
