package org.eea.dataflow.integration.executor.fme.repository;

import org.eea.dataflow.integration.executor.fme.domain.FMECollection;
import org.eea.dataflow.integration.executor.fme.domain.FMEStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "FMEInterface", url = "https://fme.discomap.eea.europa.eu/fmerest/v3")
public interface FMEFeignRepository {

  // Repository: ReportNetTesting
  @RequestMapping(value = "/healthcheck", method = RequestMethod.GET)
  FMEStatus find();


  @RequestMapping(value = "/repositories/{repository}/items", method = RequestMethod.GET)
  FMECollection findItems(@PathVariable(value = "repository") String repository);


}
