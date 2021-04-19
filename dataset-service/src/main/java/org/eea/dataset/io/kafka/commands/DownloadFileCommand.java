package org.eea.dataset.io.kafka.commands;

import org.eea.exception.EEAException;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class DownloadFileCommand extends AbstractEEAEventHandlerCommand {


  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DownloadFileCommand.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The path public file. */
  @Value("${pathPublicFile}")
  private String pathPublicFile;

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.DOWNLOAD_EXPORT_EVENT;
  }

  /**
   * Execute.
   *
   * @param eeaEventVO the eea event VO
   * @throws EEAException the EEA exception
   */
  @Override
  public void execute(EEAEventVO eeaEventVO) throws EEAException {
    // public ResponseEntity<InputStreamResource>
    Long datasetId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("dataset_id")));
    String fileName = eeaEventVO.getData().get("fileName").toString();
    LOG.info("entro para descargar el fichero del dataset {}", datasetId);



    // try {
    // File file = new File(new File(pathPublicFile, "dataset-" + datasetId), fileName);
    // if (!file.exists()) {
    // throw new EEAException(EEAErrorMessage.FILE_NOT_FOUND);
    // }
    // InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
    // HttpHeaders header = new HttpHeaders();
    // header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
    // return ResponseEntity.ok().headers(header).contentLength(file.length())
    // .contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
    // } catch (IOException | EEAException e) {
    // LOG_ERROR.error("File doesn't exist in the route {} ", fileName);
    // return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    // }


  }

}
