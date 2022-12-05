package org.eea.interfaces.controller.validation;

import org.eea.interfaces.vo.dataset.FailedValidationsDatasetVO;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.validation.ProcessTaskVO;
import org.eea.interfaces.vo.validation.TaskVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.math.BigInteger;
import java.util.List;


/**
 * The Interface ValidationController.
 */
public interface ValidationController {

  /**
   * The Interface ValidationControllerZuul.
   */
  @FeignClient(value = "validation", path = "/validation")
  interface ValidationControllerZuul extends ValidationController {

  }

  /**
   * Executes the validation job
   *
   * @param datasetId the dataset id
   * @param released the released
   * @param jobId the jobId
   * @return
   */
  @PutMapping(value = "/dataset/{id}")
  void validateDataSetData(@PathVariable("id") Long datasetId,
                           @RequestParam(value = "released", required = false) boolean released, @RequestParam(value = "jobId") Long jobId);

  /**
   * Gets the failed validations by id dataset.
   *
   * @param datasetId the dataset id
   * @param pageNum the page num
   * @param pageSize the page size
   * @param fields the fields
   * @param asc the asc
   * @param levelErrorsFilter the level errors filter
   * @param typeEntitiesFilter the type entities filter
   * @param tableFilter the table filter
   * @param fieldValueFilter the field value filter
   * @return the failed validations by id dataset
   */
  @GetMapping(value = "/listValidations/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  FailedValidationsDatasetVO getFailedValidationsByIdDataset(@PathVariable("id") Long datasetId,
      @RequestParam(value = "pageNum", defaultValue = "0", required = false) Integer pageNum,
      @RequestParam(value = "pageSize", defaultValue = "20", required = false) Integer pageSize,
      @RequestParam(value = "fields", required = false) String fields,
      @RequestParam(value = "asc", defaultValue = "true") boolean asc,
      @RequestParam(value = "levelErrorsFilter",
          required = false) List<ErrorTypeEnum> levelErrorsFilter,
      @RequestParam(value = "typeEntitiesFilter",
          required = false) List<EntityTypeEnum> typeEntitiesFilter,
      @RequestParam(value = "tableFilter", required = false) String tableFilter,
      @RequestParam(value = "fieldValueFilter", required = false) String fieldValueFilter);

  /**
   * Gets the group failed validations by id dataset.
   *
   * @param datasetId the dataset id
   * @param pageNum the page num
   * @param pageSize the page size
   * @param fields the fields
   * @param asc the asc
   * @param levelErrorsFilter the level errors filter
   * @param typeEntitiesFilter the type entities filter
   * @param tableFilter the table filter
   * @param fieldValueFilter the field value filter
   * @return the group failed validations by id dataset
   */
  @GetMapping(value = "/listGroupValidations/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  FailedValidationsDatasetVO getGroupFailedValidationsByIdDataset(
      @PathVariable("id") Long datasetId,
      @RequestParam(value = "pageNum", defaultValue = "0", required = false) Integer pageNum,
      @RequestParam(value = "pageSize", defaultValue = "20", required = false) Integer pageSize,
      @RequestParam(value = "fields", required = false) String fields,
      @RequestParam(value = "asc", defaultValue = "true") boolean asc,
      @RequestParam(value = "levelErrorsFilter",
          required = false) List<ErrorTypeEnum> levelErrorsFilter,
      @RequestParam(value = "typeEntitiesFilter",
          required = false) List<EntityTypeEnum> typeEntitiesFilter,
      @RequestParam(value = "tableFilter", required = false) String tableFilter,
      @RequestParam(value = "fieldValueFilter", required = false) String fieldValueFilter);


  /**
   * Export validation data CSV.
   *
   * @param datasetId the dataset id
   */
  @PostMapping(value = "/export/{datasetId}")
  void exportValidationDataCSV(@PathVariable("datasetId") Long datasetId);

  /**
   * Download file.
   *
   * @param datasetId the dataset id
   * @param fileName the file name
   * @param response the response
   */
  @GetMapping("/downloadFile/{datasetId}")
  void downloadFile(@PathVariable Long datasetId, @RequestParam String fileName,
      HttpServletResponse response);

  /**
   * Sets the status to IN_QUEUE for a given task id
   * @param taskId
   */
  @PutMapping(value = "/restartTask/{taskId}")
  void restartTask(@PathVariable("taskId") Long taskId);

  /**
   * Lists the task ids of tasks that are in progress for more than the specified period of time
   * @param timeInMinutes
   * @return
   */
  @GetMapping(value = "/listTasksInProgress/{timeInMinutes}")
  List<BigInteger> listTasksInProgress(@PathVariable("timeInMinutes") long timeInMinutes);

  /**
   * Saves task
   * @param taskVO
   * @return
   */
  @PutMapping(value = "/saveTask")
  TaskVO saveTask(@RequestBody TaskVO taskVO);


  /**
   * Updates task
   * @param taskVO
   * @return
   */
  @PutMapping(value = "/updateTask")
  void updateTask(@RequestBody TaskVO taskVO);

  /**
   * Finds task by json
   * @param json
   * @return
   */
  @GetMapping(value = "/findReleaseTaskByJson")
  TaskVO findReleaseTaskByJson(@RequestParam("json") String json);

  /**
   * Finds task by splitFileName
   * @param processId
   * @return
   */
  @GetMapping(value = "/private/findTasksByProcessId/{processId}")
  List<TaskVO> findTasksByProcessId(@PathVariable("processId") String processId);

  /**
   * Finds tasks by datasetId for in progress process
   * @param datasetId
   * @return
   */
  @GetMapping(value = "/private/releaseTasksByDatasetId/{datasetId}")
  List<ProcessTaskVO> findReleaseTasksByDatasetId(@PathVariable("datasetId") Long datasetId);
}
