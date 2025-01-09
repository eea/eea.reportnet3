package org.eea.dataset.controller;

import io.swagger.annotations.ApiOperation;
import org.eea.dataset.service.ReleaseReceiptService;
import org.eea.interfaces.vo.dataset.ReleaseReceiptVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * RESTful Controller for managing Release Receipts.
 */
@RestController
@RequestMapping("/release-receipts")
public class ReleaseReceiptController {

    @Autowired
    private ReleaseReceiptService releaseReceiptService;

    /**
     * Create a new ReleaseReceiptText.
     *
     * @param releaseReceiptVO the VO containing user custom text
     * @return a confirmation message
     */
    @PostMapping
    public ResponseEntity<String> createReleaseReceiptText(@RequestBody ReleaseReceiptVO releaseReceiptVO) {
        releaseReceiptService.saveReleaseReceiptText(releaseReceiptVO);
        return ResponseEntity.ok("ReleaseReceiptText created successfully!");
    }

    /**
     * Retrieve a specific ReleaseReceiptText by ID.
     *
     * @param id the ID of the release receipt
     * @return the ReleaseReceiptTextVO
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    @ApiOperation(value = "Get release receipt by Id", hidden = true)
    public ResponseEntity<ReleaseReceiptVO> getReleaseReceiptText(@PathVariable("id") Long id) {
        ReleaseReceiptVO releaseReceiptVO = releaseReceiptService.getReleaseReceiptText(id);
        return ResponseEntity.ok(releaseReceiptVO);
    }
}
