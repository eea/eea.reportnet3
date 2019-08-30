package org.eea.interfaces.controller.dataset;

import java.util.List;
import org.eea.interfaces.vo.metabase.SnapshotVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface DatasetSnapshotController {

  @FeignClient(value = "dataset", contextId = "snapshot", path = "/snapshot")
  interface DataSetSnapshotControllerZuul extends DatasetSnapshotController {

  }

  @GetMapping(value = "/{idDataset}/listSnapshots", produces = MediaType.APPLICATION_JSON_VALUE)
  List<SnapshotVO> getSnapshotsByIdDataset(@PathVariable("idDataset") Long datasetId);


  /**
   * Creates the snapshot.
   *
   * @param datasetId the dataset id
   * @param description the description
   */
  @PostMapping(value = "/{idDataset}/snapshot/create", produces = MediaType.APPLICATION_JSON_VALUE)
  void createSnapshot(@PathVariable("idDataset") Long datasetId,
      @RequestParam("description") String description);

  /**
   * Delete snapshot.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   */
  @DeleteMapping(value = "/{idDataset}/snapshot/delete/{idSnapshot}")
  void deleteSnapshot(@PathVariable("idDataset") Long datasetId,
      @PathVariable("idSnapshot") Long idSnapshot);


  @PostMapping(value = "/{idDataset}/snapshot/restore", produces = MediaType.APPLICATION_JSON_VALUE)
  void restoreSnapshot(@PathVariable("idDataset") Long datasetId,
      @RequestParam("idSnapshot") Long idSnapshot);


  @PutMapping(value = "/{idDataset}/snapshot/release", produces = MediaType.APPLICATION_JSON_VALUE)
  void releaseSnapshot(@PathVariable("idDataset") Long datasetId,
      @RequestParam("idSnapshot") Long idSnapshot);


}
