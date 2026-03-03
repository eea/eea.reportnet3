package org.eea.dataset.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.eea.dataset.service.DataLakeDataRetrieverFactory;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.PreparationDatasetService;
import org.eea.interfaces.vo.orchestrator.JobPresignedUrlInfo;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.PreparationDatasetController;
import org.eea.interfaces.controller.orchestrator.JobController.JobControllerZuul;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.PreparationDatasetVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * The Class PreparationDatasetControllerImpl.
 */
@RestController
@RequestMapping(value = "/dataset", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "Preparation Dataset")
public class PreparationDatasetControllerImpl implements PreparationDatasetController {

    private static final Logger LOG = LoggerFactory.getLogger(PreparationDatasetControllerImpl.class);

    @Autowired
    private PreparationDatasetService preparationDatasetService;

    @Autowired
    private DatasetMetabaseService datasetMetabaseService;

    @Autowired
    private DatasetSchemaService datasetSchemaService;

    @Autowired
    private DataLakeDataRetrieverFactory dataLakeDataRetrieverFactory;


    /**
     * List preparation datasets by dataflow id and provider id.
     */
    @Override
    @HystrixCommand
    @GetMapping("/preparations")
    @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_OBSERVER','DATAFLOW_STEWARD_SUPPORT','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE','DATAFLOW_REPORTER_READ','DATAFLOW_CUSTODIAN','DATAFLOW_REQUESTER','DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ','DATAFLOW_NATIONAL_COORDINATOR') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATAFLOW',#dataflowId)) OR checkApiKey(#dataflowId,#providerId,#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_OBSERVER','DATAFLOW_STEWARD_SUPPORT','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE','DATAFLOW_REPORTER_READ','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ','DATAFLOW_NATIONAL_COORDINATOR') OR hasAnyRole('ADMIN')")
    @ApiOperation(value = "List preparation datasets")
    public List<PreparationDatasetVO> list(
            @RequestParam("dataflowId") Long dataflowId,
            @RequestParam("providerId") Long providerId,
            @RequestParam(value = "code", required = false) String code) {

        LOG.info(
                "Listing preparation datasets dataflowId={}, providerId={}",
                dataflowId, providerId);

        return preparationDatasetService
                .findPreparationDatasets(dataflowId, providerId, code);
    }

    /**
     * Create a preparation dataset.
     */
    @Override
    @HystrixCommand
    @PostMapping(value = "/preparations", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @ApiOperation(value = "Create preparation dataset")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Preparation dataset created successfully"),
            @ApiResponse(code = 400, message = "Invalid preparation dataset data"),
            @ApiResponse(code = 403, message = "User not authorized"),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    public void createPreparationDataset(@RequestBody PreparationDatasetVO vo) {

        if (vo.getDataflowId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dataflowId is required");
        }
        if (vo.getProviderId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "providerId is required");
        }
        if (StringUtils.isBlank(vo.getCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "code is required");
        }
        if (StringUtils.isBlank(vo.getDatasetName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "datasetName is required");
        }

        try {
            preparationDatasetService.createPreparationDataset(vo.getDataflowId(), vo.getParentDatasetId(), vo);
        } catch (EEAException e) {
            LOG.error("Error creating preparation dataset [dataflowId={}, providerId={}, code={}]: {}", vo.getDataflowId(), vo.getProviderId(), vo.getCode(), e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOG.error("Unexpected error creating preparation dataset", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error while creating preparation dataset");
        }
    }


    /**
     * Delete a preparation dataset by id.
     */
    @Override
    @HystrixCommand
    @DeleteMapping("/preparations/{id}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation(value = "Delete preparation dataset by id")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Preparation dataset deleted successfully"),
            @ApiResponse(code = 400, message = "Invalid preparation dataset id"),
            @ApiResponse(code = 404, message = "Preparation dataset not found"),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePreparationDatasetById(@PathVariable("id") Long preparationId) {

        if (preparationId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "preparationId is required");
        }

        LOG.info("Deleting preparation dataset id={}", preparationId);

        try {
            preparationDatasetService.deletePreparationDatasetById(preparationId);

        } catch (EEAException e) {
            LOG.error("Error deleting preparation dataset id={}", preparationId, e);

            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());

        } catch (Exception e) {
            LOG.error("Unexpected error deleting preparation dataset id={}", preparationId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error while deleting preparation dataset");
        }
    }

    @PostMapping("/createAllEligiblePreparationSets")
    @ApiOperation("Create all preparation sets that have 'isCreated=false'")
    @PreAuthorize("isAuthenticated()")
    public void createAllEligiblePreparationSets(
            @RequestParam("dataflowId") Long dataflowId,
            @RequestParam("providerId") Long providerId
    ) {
        try {
            preparationDatasetService.createAllEligiblePreparationSets(dataflowId, providerId);
        } catch (Exception e) {
            LOG.error("Could not create preparation tables for datasetId {}, providerId {}", dataflowId, providerId);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Get table data for a preparation dataset (DL).
     */
    @Override
    @GetMapping("/preparations/TableValueDatasetDL/{id}")
    @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_CUSTODIAN','DATASET_STEWARD','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATACOLLECTION_CUSTODIAN','DATASCHEMA_CUSTODIAN','DATASCHEMA_STEWARD','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','DATASET_NATIONAL_COORDINATOR','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','EUDATASET_STEWARD_SUPPORT','DATACOLLECTION_OBSERVER','DATACOLLECTION_STEWARD_SUPPORT','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','DATACOLLECTION_STEWARD','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD_SUPPORT','REFERENCEDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD') OR hasAnyRole('ADMIN') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATASET',#datasetId))")
    @ApiOperation(value = "Get preparation table data", hidden = true)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved preparation data"),
            @ApiResponse(code = 400, message = "Preparation id or table schema incorrect"),
            @ApiResponse(code = 404, message = "Preparation dataset not found"),
            @ApiResponse(code = 500, message = "Error retrieving preparation data")
    })
    public TableVO getPreparationTableValuesDL(
            @ApiParam(type = "Long", value = "Preparation dataset id")
            @PathVariable("id") Long datasetId,
            @RequestParam("code") String preparationCode,
            @ApiParam(type = "String", value = "Table schema id")
            @RequestParam("idTableSchema") String idTableSchema,

            @ApiParam(type = "Integer", value = "Page number")
            @RequestParam(value = "pageNum", defaultValue = "0", required = false)
            Integer pageNum,

            @ApiParam(type = "Integer", value = "Page size")
            @RequestParam(value = "pageSize", required = false)
            Integer pageSize,

            @ApiParam(type = "String", value = "Field names")
            @RequestParam(value = "fields", required = false)
            String fields,

            @ApiParam(value = "Level error to filter")
            @RequestParam(value = "levelError", required = false)
            ErrorTypeEnum[] levelError,

            @ApiParam(value = "List of rule ids to filter")
            @RequestParam(value = "idRules", required = false)
            String[] idRules,

            @ApiParam(type = "String", value = "Field schema id")
            @RequestParam(value = "fieldSchemaId", required = false)
            String fieldSchemaId,

            @ApiParam(type = "String", value = "Value to filter")
            @RequestParam(value = "fieldValue", required = false)
            String fieldValue,

            @ApiParam(value = "List of qc codes to filter")
            @RequestParam(value = "qcCodes", required = false)
            String[] qcCodes) {
        if (null == preparationCode || null == idTableSchema) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    EEAErrorMessage.DATASET_INCORRECT_ID);
        }

        // check if the parameters received from the frontend are the needed to get the table values
        // WITHOUT PAGINATION
        Pageable pageable = null;
        if (pageSize != null) {
            pageable = PageRequest.of(pageNum, pageSize);
        }
        // else pageable will be null, it will be created inside the service
        TableVO result = null;
        try {
            DataSetMetabaseVO dataset = datasetMetabaseService.findDatasetMetabase(datasetId);
            String datasetSchemaId = dataset.getDatasetSchema();
            TableSchemaVO tableSchemaVO = datasetSchemaService.getTableSchemaVO(idTableSchema, datasetSchemaId);
            result = dataLakeDataRetrieverFactory.getRetriever(datasetId).getPreparationTableResult(dataset, tableSchemaVO, pageable, fields, fieldSchemaId, fieldValue, levelError, qcCodes, preparationCode);
        } catch (EEAException e) {
            LOG.error(e.getMessage());
            if (e.getMessage().equals(EEAErrorMessage.DATASET_NOTFOUND)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.DATASET_NOTFOUND);
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    EEAErrorMessage.OBTAINING_TABLE_DATA);
        } catch (Exception e) {
            LOG.error("Unexpected error! Error retrieving big data table values for datasetId {} and tableSchemaId {} Message: {}", datasetId, idTableSchema, e.getMessage());
            throw e;
        }

        return result;
    }
}
