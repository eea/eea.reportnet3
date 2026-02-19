package org.eea.interfaces.controller.dataset;

import org.eea.interfaces.vo.dataset.PreparationDatasetResponseVO;
import org.eea.interfaces.vo.dataset.PreparationDatasetVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.orchestrator.JobPresignedUrlInfo;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * The Interface PreparationDatasetController.
 */
public interface PreparationDatasetController {

    /**
     * The Interface PreparationDatasetControllerZuul.
     */
    interface PreparationDatasetControllerZuul extends PreparationDatasetController {
    }

    @GetMapping(
            value = "/preparations",
            produces = MediaType.APPLICATION_JSON_VALUE)
    PreparationDatasetResponseVO list(
            @RequestParam("dataflowId") Long dataflowId,
            @RequestParam("providerId") Long providerId,
            @RequestParam("code") String code);

    @PostMapping(value = "/preparations", consumes = MediaType.APPLICATION_JSON_VALUE)
    void createPreparationDataset(@RequestBody PreparationDatasetVO vo);

    @DeleteMapping("/preparations/{id}")
    void deletePreparationDatasetById(@PathVariable("id") Long preparationId);

    @PostMapping("/createAllEligiblePreparationSets")
    void createAllEligiblePreparationSets(@RequestParam("dataflowId") Long dataflowId, @RequestParam("providerId") Long providerId);


    @GetMapping("/preparations/TableValueDatasetDL/{id}")
    TableVO getPreparationTableValuesDL(
            @PathVariable("id") Long preparationId,
            @RequestParam("code") String preparationCode,
            @RequestParam("idTableSchema") String idTableSchema,
            @RequestParam(value = "pageNum", defaultValue = "0", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "fields", required = false) String fields,
            @RequestParam(value = "levelError", required = false) ErrorTypeEnum[] levelError,
            @RequestParam(value = "idRules", required = false) String[] idRules,
            @RequestParam(value = "fieldSchemaId", required = false) String fieldSchemaId,
            @RequestParam(value = "fieldValue", required = false) String fieldValue,
            @RequestParam(value = "qcCodes", required = false) String[] qcCodes
    );

    /**
     * Import big file data.
     *
     * @param datasetId the dataset id
     * @param dataflowId the dataflow id
     * @param providerId the provider id
     * @param tableSchemaId the table schema id
     * @param file the file
     * @param replace the replace
     * @param integrationId the integration id
     * @param delimiter the delimiter
     * @param jobId the jobId
     * @param fmeJobId the fmeJobId
     */
    @PostMapping("/{datasetId}/preparations/importFileData")
    Map<String, Object> importBigFileDataForPreparation(@PathVariable("datasetId") Long datasetId,
                                                        @RequestParam("code") String preparationCode,
                                                        @RequestParam(value = "dataflowId", required = false) Long dataflowId,
                                                      @RequestParam(value = "providerId", required = false) Long providerId,
                                                      @RequestParam(value = "tableSchemaId", required = false) String tableSchemaId,
                                                      @RequestParam("file") MultipartFile file,
                                                      @RequestParam(value = "replace", required = false) boolean replace,
                                                      @RequestParam(value = "integrationId", required = false) Long integrationId,
                                                      @RequestParam(value = "delimiter", required = false) String delimiter,
                                                      @RequestParam(value = "jobId", required = false) Long jobId,
                                                      @RequestParam(value = "fmeJobId", required = false) String fmeJobId) throws Exception;


    @GetMapping("/{datasetId}/preparations/generateImportPresignedUrl")
    JobPresignedUrlInfo generatePreparationImportPresignedUrl(
            @PathVariable("datasetId") Long datasetId,
            @RequestParam("code") String preparationCode,
            @RequestParam("dataflowId") Long dataflowId,
            @RequestParam(value = "providerId", required = false) Long providerId,
            @RequestParam(value = "tableSchemaId", required = false) String tableSchemaId,
            @RequestParam(value = "replace", required = false) boolean replace,
            @RequestParam(value = "integrationId", required = false) Long integrationId,
            @RequestParam(value = "delimiter", required = false) String delimiter,
            @RequestParam(value = "fileName", required = false) String fileName,
            @RequestParam(value = "etlImport", required = false, defaultValue = "false") Boolean etlImport);

}
