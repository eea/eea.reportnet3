package org.eea.dataflow.integration.fme.repository;

import org.eea.dataflow.integration.fme.domain.FMEStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "FMEInterface", url = "https://fme.discomap.eea.europa.eu/fmerest/v3")
public interface FMEFeignRepository {


  @RequestMapping(value = "/healthcheck", method = RequestMethod.GET)
  FMEStatus find();

}
