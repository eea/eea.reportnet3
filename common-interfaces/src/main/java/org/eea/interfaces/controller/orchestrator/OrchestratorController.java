package org.eea.interfaces.controller.orchestrator;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface OrchestratorController {

    /**
     * The Interface OrchestratorControllerZuul.
     */
    @FeignClient(value = "orchestrator", path = "/orchestrator")
    interface OrchestratorControllerZuul extends OrchestratorController {

    }

   @PostMapping(value = "/release/dataflow/{dataflowId}/dataProvider/{dataProviderId}")
   void release(@PathVariable(value = "dataflowId", required = true) Long dataflowId, @PathVariable(value = "dataProviderId",
            required = true) Long dataProviderId, @RequestParam(name = "restrictFromPublic", required = true,
            defaultValue = "false") boolean restrictFromPublic,
            @RequestParam(name = "validate", required = false, defaultValue = "true") boolean validate);
}
