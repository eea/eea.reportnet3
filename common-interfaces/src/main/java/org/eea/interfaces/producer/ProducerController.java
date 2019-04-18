package org.eea.interfaces.producer;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "serviceProvider")
public interface ProducerController {

    @GetMapping("/produce")
    String produceText(@RequestParam("extraText") String extraTest);
}
