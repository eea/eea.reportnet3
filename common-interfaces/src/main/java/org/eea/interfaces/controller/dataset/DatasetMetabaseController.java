package org.eea.interfaces.controller.dataset;

import java.util.List;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public interface DatasetMetabaseController {

  @FeignClient(name = "dataset", qualifier = "monguer", path = "/datasetmetabase")
  interface DataSetMetabaseControllerZuul extends DatasetMetabaseController {

  }


  @RequestMapping(value = "/dataflow/{id}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<DataSetVO> findDataSetIdByDataflowId(final @PathVariable("id") Long idDataflow);


}
