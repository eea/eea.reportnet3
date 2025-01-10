package org.eea.interfaces.controller.dataset;

import org.eea.interfaces.vo.dataset.ReleaseReceiptVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public interface ReleaseReceiptController {

    /**
     * Create a new ReleaseReceipt.
     *
     * @param releaseReceiptVO the VO containing user custom text
     * @return the saved ReleaseReceiptVO
     */
    @PostMapping
    ResponseEntity<ReleaseReceiptVO> createReleaseReceipt(@RequestBody ReleaseReceiptVO releaseReceiptVO);

    /**
     * Update a ReleaseReceipt.
     *
     * @param releaseReceiptVO the VO containing updated details
     * @return a confirmation message
     */
    @PutMapping
    ResponseEntity<ReleaseReceiptVO> updateReleaseReceipt(@RequestBody ReleaseReceiptVO releaseReceiptVO);

    /**
     * Retrieve a specific ReleaseReceipt by ID.
     *
     * @param id the ID of the release receipt
     * @return the ReleaseReceiptVO
     */
    @GetMapping("/{id}")
    ResponseEntity<ReleaseReceiptVO> getReleaseReceipt(@PathVariable("id") Long id);

    /**
     * Retrieve a specific ReleaseReceipt by Dataflow ID.
     *
     * @param dataflowId the Dataflow ID
     * @return the ReleaseReceiptVO
     */
    @GetMapping("/dataflow/{dataflowId}")
    ResponseEntity<ReleaseReceiptVO> getReleaseReceiptByDataflowId(@PathVariable("dataflowId") Long dataflowId);
}
