package org.eea.indexsearch.io.kafka;

import org.eea.indexsearch.io.kafka.commands.CreateConnectionCommand;
import org.eea.indexsearch.io.kafka.commands.DeletedTableCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.handler.EEAEventHandlerImpl;
import org.eea.kafka.interfaces.EEAEventCommandFactory;
import org.eea.kafka.interfaces.EEAEventHandlerCommand;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CommandFactory implements EEAEventCommandFactory {

  private static final Logger LOG = LoggerFactory.getLogger(CommandFactory.class);

  private static final String INDEX = "lead";
  private static final String TYPE = "lead";

  private RestHighLevelClient client;

  private ObjectMapper objectMapper;

  @Autowired
  public CommandFactory(RestHighLevelClient client, ObjectMapper objectMapper) {
    this.client = client;
    this.objectMapper = objectMapper;
  }

  @Autowired
  EEAEventHandlerImpl EEAEventHandlerImpl;

  public EEAEventHandlerCommand getEventCommand(EEAEventVO message) {

    switch (message.getEventType()) {
      case CONNECTION_CREATED_EVENT:
        LOG.info("New Connection event.");
        return new CreateConnectionCommand(client, objectMapper);
      case HELLO_KAFKA_EVENT:
        LOG.info("");
      case VALIDATION_FINISHED_EVENT:
        LOG.info("Validations Finish Event.");
      case LOAD_DATA_COMPLETED_EVENT:
        LOG.info("");
      case RECORD_UPDATED_COMPLETED_EVENT:
        LOG.info("");
      case RECORD_CREATED_COMPLETED_EVENT:
        LOG.info("");
      case RECORD_DELETED_COMPLETED_EVENT:
        LOG.info("");
      case DELETED_TABLE:
        LOG.info("Table deleted event.");
        return new DeletedTableCommand(client, objectMapper);
      case LOAD_DOCUMENT_COMPLETED_EVENT:
        LOG.info("");
      case DELETE_DOCUMENT_COMPLETED_EVENT:
        LOG.info("Delete Document event.");
      case FIELD_UPDATED_COMPLETED_EVENT:
        LOG.info("");
      case SNAPSHOT_RESTORED_EVENT:
        LOG.info("");
      default:
        System.out.println("Can't process event");
        return null;
    }
  }


}
