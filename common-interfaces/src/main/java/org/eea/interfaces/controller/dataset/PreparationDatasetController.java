package org.eea.interfaces.controller.dataset;

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

import java.util.List;
import java.util.Map;

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
    List<PreparationDatasetVO> list(
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
}
