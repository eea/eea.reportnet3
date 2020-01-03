package org.eea.interfaces.controller.dataset;

import org.eea.interfaces.vo.dataset.CodelistVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * The Interface DatasetCodelistController.
 */
public interface DatasetCodelistController {

  /**
   * The Interface DataSetCodelistControllerZuul.
   */
  @FeignClient(value = "dataset", contextId = "codelist", path = "/codelist")
  interface DataSetCodelistControllerZuul extends DatasetCodelistController {

  }

  /**
   * Gets the by id.
   *
   * @param codelistId the codelist id
   * @return the by id
   */
  @GetMapping(value = "/{codelistId}", produces = MediaType.APPLICATION_JSON_VALUE)
  CodelistVO getById(@PathVariable("codelistId") Long codelistId);

  /**
   * Creates the.
   *
   * @param codelistVO the codelist VO
   * @return the long
   */
  @PostMapping
  Long create(@RequestBody CodelistVO codelistVO);

  /**
   * Update.
   *
   * @param codelistVO the codelist VO
   * @return the long
   */
  @PutMapping(value = "/update")
  Long update(@RequestBody CodelistVO codelistVO);

  /**
   * Clone.
   *
   * @param codelistId the codelist id
   * @param codelistVO the codelist VO
   * @return the long
   */
  @PostMapping(value = "/clone/{codelistId}")
  Long clone(@PathVariable("codelistId") Long codelistId, @RequestBody CodelistVO codelistVO);

  /**
   * Delete document.
   *
   * @param codelistId the codelist id
   */
  @DeleteMapping(value = "/{codelistId}")
  void deleteDocument(@PathVariable("codelistId") Long codelistId);
}
