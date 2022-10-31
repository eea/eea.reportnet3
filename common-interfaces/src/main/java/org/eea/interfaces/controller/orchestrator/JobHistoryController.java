package org.eea.interfaces.controller.orchestrator;

import org.eea.interfaces.vo.orchestrator.JobHistoryVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/** The Interface JobHistoryController. */
public interface JobHistoryController {

    @FeignClient(value = "jobHistory", path = "/jobHistory")
    interface JobHistoryControllerZuul extends JobHistoryController {
    }

}
