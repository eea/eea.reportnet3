package org.eea.indexsearch.io.kafka.commands;

import java.io.IOException;
import org.eea.indexsearch.io.kafka.interfaces.EventCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DeletedTableCommand implements EventCommand {

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
  public void execute(EEAEventVO eeaEventVO) throws IOException {
    // TODO
  }
}


