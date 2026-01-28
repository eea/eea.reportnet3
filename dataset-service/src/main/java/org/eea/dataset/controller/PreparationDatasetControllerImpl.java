package org.eea.dataset.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.eea.dataset.service.PreparationDatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.PreparationDatasetController;
import org.eea.interfaces.vo.dataset.PreparationDatasetVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

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
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Preparation dataset created successfully"),
            @ApiResponse(code = 400, message = "Invalid preparation dataset data"),
            @ApiResponse(code = 403, message = "User not authorized"),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    public void createPreparationDataset(
            @RequestBody PreparationDatasetVO vo) {

        if (vo.getDataflowId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "dataflowId is required");
        }
        if (vo.getProviderId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "providerId is required");
        }
        if (StringUtils.isBlank(vo.getCode())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "code is required");
        }
        if (StringUtils.isBlank(vo.getDatasetName())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "datasetName is required");
        }

        try {
            preparationDatasetService.createPreparationDataset(
                    vo.getDataflowId(),
                    vo.getParentDatasetId(),
                    vo);

        } catch (EEAException e) {
            LOG.error(
                    "Error creating preparation dataset [dataflowId={}, providerId={}, code={}]: {}",
                    vo.getDataflowId(),
                    vo.getProviderId(),
                    vo.getCode(),
                    e.getMessage(),
                    e);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage());

        } catch (Exception e) {
            LOG.error("Unexpected error creating preparation dataset", e);

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unexpected error while creating preparation dataset");
        }
    }


    /**
     * Delete a preparation dataset by id.
     */
    @Override
    @HystrixCommand
    @DeleteMapping("/preparations/{id}")
    @ApiOperation(value = "Delete preparation dataset by id")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Preparation dataset deleted successfully"),
            @ApiResponse(code = 400, message = "Invalid preparation dataset id"),
            @ApiResponse(code = 404, message = "Preparation dataset not found"),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePreparationDatasetById(
            @PathVariable("id") Long preparationId) {

        if (preparationId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "preparationId is required");
        }

        LOG.info("Deleting preparation dataset id={}", preparationId);

        try {
            preparationDatasetService.deletePreparationDatasetById(preparationId);

        } catch (EEAException e) {
            LOG.error("Error deleting preparation dataset id={}", preparationId, e);

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    e.getMessage());

        } catch (Exception e) {
            LOG.error(
                    "Unexpected error deleting preparation dataset id={}",
                    preparationId,
                    e);

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unexpected error while deleting preparation dataset");
        }
    }
}
