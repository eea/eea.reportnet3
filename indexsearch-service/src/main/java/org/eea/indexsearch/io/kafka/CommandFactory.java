package org.eea.indexsearch.io.kafka;

import org.eea.indexsearch.io.kafka.commands.CreateConnectionCommand;
import org.eea.indexsearch.io.kafka.commands.DeletedTableCommand;
import org.eea.indexsearch.io.kafka.interfaces.CommandEventFactory;
import org.eea.indexsearch.io.kafka.interfaces.EventCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CommandFactory implements CommandEventFactory {

  private static final String INDEX = "lead";
  private static final String TYPE = "lead";

  private RestHighLevelClient client;

  private ObjectMapper objectMapper;

  @Autowired
  public CommandFactory(RestHighLevelClient client, ObjectMapper objectMapper) {
    this.client = client;
    this.objectMapper = objectMapper;
  }

  public EventCommand getEventCommand(EEAEventVO message) {
    switch (message.getEventType()) {
      case CONNECTION_CREATED_EVENT:
        System.out.println("New Connection");
        return new CreateConnectionCommand(client, objectMapper);
      case DELETED_TABLE:
        System.out.println("Table deleted");
        return new DeletedTableCommand(client, objectMapper);
      case VALIDATION_FINISHED_EVENT:
        System.out.println("Validations Finish");
      default:
        System.out.println("Can't process event");
        return null;
    }
  }
}
