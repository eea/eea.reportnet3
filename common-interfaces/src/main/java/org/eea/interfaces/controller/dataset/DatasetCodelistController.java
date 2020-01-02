package org.eea.interfaces.controller.dataset;

import org.springframework.cloud.openfeign.FeignClient;


/**
 * The Interface DatasetCodelistController.
 */
public interface DatasetCodelistController {

  /**
   * The Interface DataSetCodelistControllerZuul.
   */
  @FeignClient(value = "dataset", contextId = "codelist", path = "/codelist")
  interface DataSetCodelistControllerZuul extends DatasetCodelistController {

  }

  // @GetMapping(value = "/{idDataset}/listSnapshots", produces = MediaType.APPLICATION_JSON_VALUE)
  // List<SnapshotVO> getSnapshotsByIdDataset(@PathVariable("idDataset") Long datasetId);
  //
  // @PostMapping(value = "/{idDataset}/create", produces = MediaType.APPLICATION_JSON_VALUE)
  // void createSnapshot(@PathVariable("idDataset") Long datasetId,
  // @RequestParam("description") String description);
  //
  // @DeleteMapping(value = "/{idSnapshot}/dataset/{idDataset}/delete")
  // void deleteSnapshot(@PathVariable("idDataset") Long datasetId,
  // @PathVariable("idSnapshot") Long idSnapshot);
  //
  //
  // @GetMapping(value = "/dataschema/{idDesignDataset}/listSnapshots",
  // produces = MediaType.APPLICATION_JSON_VALUE)
  // List<SnapshotVO> getSchemaSnapshotsByIdDataset(@PathVariable("idDesignDataset") Long
  // datasetId);
  //
  //
  // @DeleteMapping(value = "/{idSnapshot}/dataschema/{idDesignDataset}/delete")
  // void deleteSchemaSnapshot(@PathVariable("idDesignDataset") Long datasetId,
  // @PathVariable("idSnapshot") Long idSnapshot) throws Exception;


}
