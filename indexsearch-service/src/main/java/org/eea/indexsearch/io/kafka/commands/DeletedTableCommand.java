package org.eea.indexsearch.io.kafka.commands;

import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.interfaces.EEAEventHandlerCommand;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DeletedTableCommand implements EEAEventHandlerCommand {

  private static final String INDEX = "lead";
  private static final String TYPE = "lead";

  private RestHighLevelClient client;

  private ObjectMapper objectMapper;

  @Autowired
  public DeletedTableCommand(RestHighLevelClient client, ObjectMapper objectMapper) {
    this.client = client;
    this.objectMapper = objectMapper;
  }

  @Override
  public void execute(EEAEventVO eeaEventVO) {
    // TODO
  }
}


