package org.eea.validation.controller;

import org.eea.validation.service.SqlRulesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sqlRules")
public class SQLControllerImpl {

  @Autowired
  private SqlRulesService sqlRulesService;

  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN')")
  @PutMapping(value = "/checkquery", produces = MediaType.APPLICATION_JSON_VALUE)
  public void validateDataSetData(@RequestParam("query") String query,
      @RequestParam("datasetId") Long datasetId) {

    sqlRulesService.queryTreat(query, datasetId);
  }


}
