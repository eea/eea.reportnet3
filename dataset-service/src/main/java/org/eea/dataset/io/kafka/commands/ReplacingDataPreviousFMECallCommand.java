package org.eea.dataset.io.kafka.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eea.dataset.service.helper.FileTreatmentHelper;
import org.eea.interfaces.controller.dataflow.IntegrationController.IntegrationControllerZuul;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.lock.service.LockService;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
 * The Class ReplacingDataPreviousFMECallCommand.
 */
@Component
public class ReplacingDataPreviousFMECallCommand extends AbstractEEAEventHandlerCommand {


  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


  /** The import path. */
  @Value("${importPath}")
  private String importPath;

  /** The integration controller. */
  @Autowired
  private IntegrationControllerZuul integrationController;

  /** The lock service. */
  @Autowired
  private LockService lockService;

  /** The file treatment helper. */
  @Autowired
  private FileTreatmentHelper fileTreatmentHelper;


  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.CONTINUE_FME_PROCESS_EVENT;
  }

  /**
   * Perform action.
   *
   * @param eeaEventVO the eea event VO
   */
  @Override
  public void execute(EEAEventVO eeaEventVO) {
    Long datasetId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("datasetId")));
    String fileName = eeaEventVO.getData().get("fileName").toString();
    File file = new File(fileName);
    Long integrationId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("integrationId")));
    IntegrationVO integrationVO = integrationController.findIntegrationById(integrationId);
    boolean error = false;

    try (InputStream inputStream = new FileInputStream(file)) {
      // TODO. Encode and copy the file content into the IntegrationVO. This method load the entire
      // file in memory. To solve it, the FME connector should be redesigned.
      byte[] byteArray = IOUtils.toByteArray(inputStream);
      String encodedString = Base64.getEncoder().encodeToString(byteArray);
      Map<String, String> externalParameters = new HashMap<>();
      externalParameters.put("fileIS", encodedString);
      integrationVO.setExternalParameters(externalParameters);

      if ((Integer) integrationController
          .executeIntegrationProcess(IntegrationToolTypeEnum.FME,
              IntegrationOperationTypeEnum.IMPORT, file.getName(), datasetId, integrationVO)
          .getExecutionResultParams().get("id") == 0) {
        error = true;
      }

      FileUtils.deleteDirectory(new File(importPath, datasetId.toString()));
    } catch (IOException e) {
      LOG_ERROR.error(
          "Error processing the call to FME executing integration: datasetId={}, fileName={}, IntegrationVO={}",
          datasetId, file.getName(), integrationVO);
      manageLock(datasetId);
    }

    if (error) {
      LOG_ERROR.error("Error executing integration: datasetId={}, fileName={}, IntegrationVO={}",
          datasetId, file.getName(), integrationVO);
      manageLock(datasetId);
    }
  }


  /**
   * Manage lock.
   *
   * @param datasetId the dataset id
   */
  private void manageLock(Long datasetId) {
    Map<String, Object> importFileData = new HashMap<>();
    importFileData.put(LiteralConstants.SIGNATURE, LockSignature.IMPORT_FILE_DATA.getValue());
    importFileData.put(LiteralConstants.DATASETID, datasetId);
    lockService.removeLockByCriteria(importFileData);
    fileTreatmentHelper.releaseLockReleasingProcess(datasetId);
  }


}
