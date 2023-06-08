package org.eea.interfaces.controller.dataset;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

public interface BigDataDatasetController {

    /**
     * The Interface BigDataDatasetControllerZuul.
     */
    @FeignClient(value = "bigDataDataset", path = "/bigDataDataset")
    interface BigDataDatasetControllerZuul extends BigDataDatasetController {
    }

    /**
     * Import big data.
     *
     * @param datasetId the dataset id
     * @param dataflowId the dataflow id
     * @param providerId the provider id
     * @param tableSchemaId the table schema id
     * @param file the file
     * @param replace the replace
     * @param integrationId the integration id
     * @param delimiter the delimiter
     * @param fmeJobId the fmeJobId
     */
    @PostMapping("/importBigData/{datasetId}")
    void importBigData(@PathVariable("datasetId") Long datasetId,
                           @RequestParam(value = "dataflowId", required = false) Long dataflowId,
                           @RequestParam(value = "providerId", required = false) Long providerId,
                           @RequestParam(value = "tableSchemaId", required = false) String tableSchemaId,
                           @RequestParam("file") MultipartFile file,
                           @RequestParam(value = "replace", required = false) boolean replace,
                           @RequestParam(value = "integrationId", required = false) Long integrationId,
                           @RequestParam(value = "delimiter", required = false) String delimiter,
                           @RequestParam(value = "fmeJobId", required = false) String fmeJobId);
}
