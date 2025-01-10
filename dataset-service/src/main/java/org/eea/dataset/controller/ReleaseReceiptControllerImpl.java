package org.eea.dataset.controller;

import io.swagger.annotations.ApiOperation;
import org.eea.dataset.service.ReleaseReceiptService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.ReleaseReceiptController;
import org.eea.interfaces.vo.dataset.ReleaseReceiptVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


/**
 * RESTful Controller for managing Release Receipts.
 */
@RestController
@RequestMapping("/release-receipts")
public class ReleaseReceiptControllerImpl implements ReleaseReceiptController {

    @Autowired
    private ReleaseReceiptService releaseReceiptService;

    private static final Logger LOG = LoggerFactory.getLogger(ReleaseReceiptControllerImpl.class);

    /**
     * Create a new ReleaseReceipt.
     *
     * @param releaseReceiptVO the VO containing user custom text
     * @return a confirmation message
     */
    @PostMapping
    public ResponseEntity<ReleaseReceiptVO> createReleaseReceipt(@RequestBody ReleaseReceiptVO releaseReceiptVO) {
        ReleaseReceiptVO savedReceipt = null;
        try {

            if (releaseReceiptVO.getDataflowId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dataflowId is required");
            }

            savedReceipt = releaseReceiptService.saveOrUpdateReleaseReceipt(releaseReceiptVO);

        } catch (EEAException e) {
            LOG.error("Error creating new release receipt for dataflowId {}: {}", releaseReceiptVO.getDataflowId(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.CREATING_RELEASE_RECEIPT);
        } catch (Exception e) {
            LOG.error("Unexpected error! Could not create release receipt for dataflowId {}: {}", releaseReceiptVO.getDataflowId(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while processing your request.");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(savedReceipt);
    }


    /**
     * Update a ReleaseReceipt.
     *
     * @param releaseReceiptVO the VO
     * @return a confirmation message
     */
    @PutMapping
    public ResponseEntity<ReleaseReceiptVO> updateReleaseReceipt(@RequestBody ReleaseReceiptVO releaseReceiptVO) {
        try {
            if (releaseReceiptVO.getDataflowId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dataflowId is required for updating a release receipt");
            }

            ReleaseReceiptVO updatedReceipt = releaseReceiptService.saveOrUpdateReleaseReceipt(releaseReceiptVO);

            return ResponseEntity.ok(updatedReceipt);

        } catch (EEAException e) {
            // Log and handle application-specific exception
            LOG.error("Error updating release receipt for dataflowId {}: {}", releaseReceiptVO.getDataflowId(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.UPDATING_RELEASE_RECEIPT);
        } catch (Exception e) {
            // Log and handle unexpected exceptions
            LOG.error("Unexpected error! Could not update release receipt for dataflowId {}: {}", releaseReceiptVO.getDataflowId(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while updating the release receipt.");
        }
    }


    /**
     * Retrieve a specific ReleaseReceipt by ID.
     *
     * @param id the ID of the release receipt
     * @return the ReleaseReceiptVO
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    @ApiOperation(value = "Get release receipt by Id", hidden = true)
    public ResponseEntity<ReleaseReceiptVO> getReleaseReceipt(@PathVariable("id") Long id) {
        try {
            ReleaseReceiptVO releaseReceiptVO = releaseReceiptService.getReleaseReceipt(id);

            return ResponseEntity.ok(releaseReceiptVO);

        } catch (EEAException e) {
            // Log the application-specific error
            LOG.error("Error retrieving release receipt with ID {}: {}", id, e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Release receipt not found for ID: " + id);
        } catch (Exception e) {
            // Log unexpected exceptions
            LOG.error("Unexpected error! Could not retrieve release receipt with ID {}: {}", id, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
        }
    }


    /**
     * Retrieve a ReleaseReceipt by Dataflow ID.
     *
     * @param dataflowId the Dataflow ID
     * @return the ReleaseReceiptVO
     */
    @GetMapping("/dataflow/{dataflowId}")
    public ResponseEntity<ReleaseReceiptVO> getReleaseReceiptByDataflowId(@PathVariable("dataflowId") Long dataflowId) {
        if (dataflowId == null || dataflowId <= 0) {
            LOG.error("Invalid dataflowId: {}", dataflowId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid dataflowId: " + dataflowId);
        }

        try {
            ReleaseReceiptVO releaseReceiptVO = releaseReceiptService.getReleaseReceiptByDataflowId(dataflowId);

            return ResponseEntity.ok(releaseReceiptVO);

        } catch (EEAException e) {
            LOG.error("Error retrieving release receipt for dataflowId {}: {}", dataflowId, e.getMessage(), e);

            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Release receipt not found for dataflowId: " + dataflowId);
        } catch (Exception e) {
            LOG.error("Unexpected error while retrieving release receipt for dataflowId {}: {}", dataflowId, e.getMessage(), e);

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
        }
    }
}
