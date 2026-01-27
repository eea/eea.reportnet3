package org.eea.dataset.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;

import org.eea.dataset.service.PreparationDatasetService;
import org.eea.interfaces.controller.dataset.PreparationDatasetController;
import org.eea.interfaces.vo.dataset.PreparationDatasetVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * The Class PreparationDatasetControllerImpl.
 */
@RestController
@RequestMapping(
        value = "/dataset",
        produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "Preparation Dataset")
public class PreparationDatasetControllerImpl
        implements PreparationDatasetController {

    private static final Logger LOG =
            LoggerFactory.getLogger(PreparationDatasetControllerImpl.class);

    @Autowired
    private PreparationDatasetService preparationDatasetService;

    /**
     * List preparation datasets by dataflow id and provider id.
     */
    @Override
    @HystrixCommand
    @GetMapping("/preparations")
    @ApiOperation(value = "List preparation datasets")
    public List<PreparationDatasetVO> list(
            @RequestParam("dataflowId") Long dataflowId,
            @RequestParam("providerId") Long providerId) {

        LOG.info(
                "Listing preparation datasets dataflowId={}, providerId={}",
                dataflowId, providerId);

        return preparationDatasetService
                .findPreparationDatasets(dataflowId, providerId);
    }

    /**
     * Create a preparation dataset.
     */
    @Override
    @HystrixCommand
    @PostMapping(
            value = "/preparations",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Create preparation dataset")
    public void createPreparationDataset(
            @RequestBody PreparationDatasetVO vo) {

        preparationDatasetService.createPreparationDataset(
                vo.getDataflowId(),
                vo.getParentDatasetId(),
                vo);
    }

    /**
     * Delete a preparation dataset by id.
     */
    @Override
    @HystrixCommand
    @DeleteMapping("/preparations/{id}")
    @ApiOperation(value = "Delete preparation dataset by id")
    public void deletePreparationDatasetById(
            @PathVariable("id") Long preparationId) {

        LOG.info("Deleting preparation dataset id={}", preparationId);

        preparationDatasetService.deletePreparationDatasetById(preparationId);
    }

    /**
     * Delete all preparation datasets by dataflow id and provider id.
     */
    @Override
    @HystrixCommand
    @DeleteMapping("/preparations")
    @ApiOperation(value = "Delete preparation datasets by dataflow and provider")
    public void deleteAllPreparationDatasetsByProviderAndDataflowId(
            @RequestParam("dataflowId") Long dataflowId,
            @RequestParam("providerId") Long providerId) {

        LOG.info(
                "Deleting preparation datasets dataflowId={}, providerId={}",
                dataflowId, providerId);

        preparationDatasetService
                .deleteAllPreparationDatasetsByProviderAndDataflowId(dataflowId, providerId);
    }
}
