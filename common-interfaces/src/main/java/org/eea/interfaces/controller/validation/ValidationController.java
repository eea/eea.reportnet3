package org.eea.interfaces.controller.validation;

import org.eea.interfaces.vo.dataset.FailedValidationsDatasetVO;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.metabase.TaskType;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.validation.TaskVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;


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
   * Lists the tasks of validation tasks that are in progress for more than the specified period of time
   * @param timeInMinutes
   * @return
   */
  @GetMapping(value = "/listInProgressValidationTasks/{timeInMinutes}")
  List<BigInteger> listInProgressValidationTasksThatExceedTime(@PathVariable("timeInMinutes") long timeInMinutes);

  /**
   * Deletes the locks related to release
   * @param datasetId
   * @return
   */
  @DeleteMapping(value = "/deleteLocksToReleaseProcess/{datasetId}")
  void deleteLocksToReleaseProcess(@PathVariable("datasetId") Long datasetId);

  /**
   * Finds tasks by processId
   * @param processId
   * @return
   */
  @GetMapping(value = "/private/findTasksByProcessId/{processId}")
  List<BigInteger> findTasksByProcessId(@PathVariable("processId") String processId);

  /**
   * Finds if tasks exist by processId and status
   * @param processId
   * @return
   */
   @GetMapping(value = "/private/findIfTasksExistByProcessIdAndStatusAndDuration/{processId}")
   Boolean findIfTasksExistByProcessIdAndStatusAndDuration(@PathVariable("processId") String processId, @RequestParam("status") ProcessStatusEnum status, @RequestParam("maxDuration") Long maxDuration);

  /**
   * Updates task status based on process id and current status
   *
   * @param status the status
   * @param processId the process id
   * @param currentStatuses the list of statuses
   */
  @PostMapping(value = "/private/updateTaskStatusByProcessIdAndCurrentStatuses/{processId}")
  void updateTaskStatusByProcessIdAndCurrentStatuses(@PathVariable("processId") String processId,  @RequestParam("status") ProcessStatusEnum status, @RequestParam("statuses") Set<String> currentStatuses);

  /**
   * Finds tasks by processId and status
   * @param processId
   * @param status
   * @return
   */
  @GetMapping(value = "/private/findTasksCountByProcessIdAndStatusIn/{processId}")
  Integer findTasksCountByProcessIdAndStatusIn(@PathVariable("processId") String processId, @RequestParam("status") List<String> status);

  /**
   * Finds the latest task that is in a specific status for more than timeInMinutes minutes
   * @param processId
   * @param timeInMinutes
   * @param statuses
   * @param taskType
   * @return
   */
  @GetMapping(value = "/private/getTaskThatExceedsTimeByStatusesAndType")
  TaskVO getTaskThatExceedsTimeByStatusesAndType(@RequestParam("processId") String processId, @RequestParam("timeInMinutes") long timeInMinutes,
                                                      @RequestParam("statuses") Set<String> statuses, @RequestParam("taskType") TaskType taskType);

  /**
   * Executes validation
   * @param datasetId
   * @param processId
   * @param released
   * @param updateViews
   * @throws Exception
   */
  @PutMapping("/private/executeValidation/{datasetId}")
  void executeValidation(@PathVariable("datasetId") Long datasetId, @RequestParam("processId") String processId, @RequestParam("released") boolean released, @RequestParam("updateViews") boolean updateViews) throws Exception;

  /**
   * Finds task by taskId
   * @param taskId
   * @return
   */
  @GetMapping("/private/findTaskById/{taskId}")
  TaskVO findTaskById(@PathVariable("taskId") Long taskId);

  /**
   * Finds task by processId
   * @param processId
   * @return
   */
  @GetMapping("/private/hasProcessCanceledTasks/{processId}")
  boolean hasProcessCanceledTasks(@PathVariable("processId") String processId);
}
















