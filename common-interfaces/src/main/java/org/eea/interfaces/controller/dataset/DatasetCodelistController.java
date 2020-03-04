package org.eea.interfaces.controller.dataset;

import java.util.List;
import org.eea.interfaces.vo.dataset.CodelistCategoryFullVO;
import org.eea.interfaces.vo.dataset.CodelistCategoryVO;
import org.eea.interfaces.vo.dataset.CodelistVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * The Interface DatasetCodelistController.
 */
@Deprecated
public interface DatasetCodelistController {

  /**
   * The Interface DataSetCodelistControllerZuul.
   */
  @FeignClient(value = "dataset", contextId = "codelist", path = "/codelist")
  @Deprecated
  interface DataSetCodelistControllerZuul extends DatasetCodelistController {

  }

  /**
   * Gets the by id.
   *
   * @param codelistId the codelist id
   * @return the by id
   */
  @GetMapping(value = "/{codelistId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Deprecated
  CodelistVO getById(@PathVariable("codelistId") Long codelistId);

  /**
   * Creates the.
   *
   * @param codelistVO the codelist VO
   * @return the long
   */
  @PostMapping
  @Deprecated
  Long create(@RequestBody CodelistVO codelistVO);

  /**
   * Update.
   *
   * @param codelistVO the codelist VO
   * @return the long
   */
  @PutMapping(value = "/update")
  @Deprecated
  Long update(@RequestBody CodelistVO codelistVO);

  /**
   * Clone.
   *
   * @param codelistId the codelist id
   * @param codelistVO the codelist VO
   * @return the long
   */
  @PostMapping(value = "/clone/{codelistId}")
  @Deprecated
  Long clone(@PathVariable("codelistId") Long codelistId, @RequestBody CodelistVO codelistVO);

  /**
   * Delete codelist.
   *
   * @param codelistId the codelist id
   */
  @DeleteMapping(value = "/{codelistId}")
  @Deprecated
  void delete(@PathVariable("codelistId") Long codelistId);

  /**
   * Gets the category by id.
   *
   * @param codelistCategoryId the codelist category id
   * @return the category by id
   */
  @GetMapping(value = "/category/{codelistCategoryId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Deprecated
  CodelistCategoryVO getCategoryById(@PathVariable("codelistCategoryId") Long codelistCategoryId);

  /**
   * Gets the all categories.
   *
   * @return the all categories
   */
  @GetMapping(value = "/category/all", produces = MediaType.APPLICATION_JSON_VALUE)
  @Deprecated
  List<CodelistCategoryVO> getAllCategories();

  /**
   * Creates the category.
   *
   * @param codelistCategoryVO the codelist category VO
   * @return the long
   */
  @PostMapping(value = "/category")
  @Deprecated
  Long createCategory(@RequestBody CodelistCategoryVO codelistCategoryVO);

  /**
   * Update category.
   *
   * @param codelistCategoryVO the codelist category VO
   * @return the long
   */
  @PutMapping(value = "/category/update")
  @Deprecated
  Long updateCategory(@RequestBody CodelistCategoryVO codelistCategoryVO);

  /**
   * Delete category.
   *
   * @param codelistCategoryId the codelist category id
   */
  @DeleteMapping(value = "/category/{codelistCategoryId}")
  @Deprecated
  void deleteCategory(@PathVariable("codelistCategoryId") Long codelistCategoryId);

  /**
   * Gets the all by id.
   *
   * @param codelistIds the codelist ids
   * @return the all by id
   */
  @GetMapping(value = "/find", produces = MediaType.APPLICATION_JSON_VALUE)
  @Deprecated
  List<CodelistVO> getAllById(@RequestParam(value = "codelistIds") String codelistIds);

  /**
   * Gets the all by category id.
   *
   * @param codelistCategoryId the codelist category id
   * @return the all by category id
   */
  @GetMapping(value = "/find/category/{codelistCategoryId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Deprecated
  List<CodelistVO> getAllByCategoryId(@PathVariable("codelistCategoryId") Long codelistCategoryId);

  /**
   * Gets the all categories complete.
   *
   * @return the all categories complete
   */
  @GetMapping(value = "/category/complete", produces = MediaType.APPLICATION_JSON_VALUE)
  @Deprecated
  List<CodelistCategoryFullVO> getAllCategoriesComplete();
}
