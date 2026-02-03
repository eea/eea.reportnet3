package org.eea.interfaces.controller.dataset;

import java.util.List;
import org.eea.interfaces.vo.dataset.PreparationDatasetVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * The Interface PreparationDatasetController.
 */
public interface PreparationDatasetController {

    /**
     * The Interface PreparationDatasetControllerZuul.
     */
    interface PreparationDatasetControllerZuul
            extends PreparationDatasetController {
    }

    @GetMapping(
            value = "/preparations",
            produces = MediaType.APPLICATION_JSON_VALUE)
    List<PreparationDatasetVO> list(
            @RequestParam("dataflowId") Long dataflowId,
            @RequestParam("providerId") Long providerId,
            @RequestParam("code") String code);

    @PostMapping(
            value = "/preparations",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    void createPreparationDataset(@RequestBody PreparationDatasetVO vo);

    @DeleteMapping("/preparations/{id}")
    void deletePreparationDatasetById(
            @PathVariable("id") Long preparationId);
}
