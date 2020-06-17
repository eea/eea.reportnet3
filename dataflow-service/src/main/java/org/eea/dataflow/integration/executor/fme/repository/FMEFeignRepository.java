package org.eea.dataflow.integration.executor.fme.repository;

import org.eea.dataflow.integration.executor.fme.configuration.FMEConfiguration;
import org.eea.dataflow.integration.executor.fme.domain.FMEAsyncJob;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@FeignClient(name = "FMEInterface", contextId = "FMEInterface", url = "${integration.fme.url}",
    configuration = FMEConfiguration.class, primary = false)
public interface FMEFeignRepository {

  // https://fme.discomap.eea.europa.eu/fmerest/v3

  @PostMapping(value = "/transformations/submit/{repository}/{workspace}")
  public Integer submitAsyncJob(@PathVariable(value = "repository") String repository,
      @PathVariable(value = "workspace") String workspace, @RequestBody FMEAsyncJob fmeAsyncJob);


}
