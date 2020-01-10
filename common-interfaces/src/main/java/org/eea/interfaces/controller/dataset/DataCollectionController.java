package org.eea.interfaces.controller.dataset;

import java.util.List;
import org.eea.interfaces.vo.dataset.DataCollectionVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface DataCollectionController {

  @FeignClient(value = "dataset", contextId = "datacollection", path = "/datacollection")
  interface DataCollectionControllerZuul extends DataCollectionController {

  }


  @PostMapping(value = "/create")
  void createEmptyDataCollection(@RequestBody DataCollectionVO dataCollectionVO);


  @GetMapping(value = "/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  List<DataCollectionVO> findDataCollectionIdByDataflowId(
      @PathVariable("id") final Long idDataflow);

}
