package org.eea.indexsearch.io.kafka.commands;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.eea.indexsearch.io.kafka.domain.ElasticSearchData;
import org.eea.indexsearch.io.kafka.domain.EntityEvent;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class EventHandlerCommand. Event Handler Command where we are encapsulating both
 * Object[EventHandlerReceiver] and the operation[Close] together as command.
 * 
 */
@Component
public class CreateConnectionCommand extends AbstractEEAEventHandlerCommand {


  /** The Constant INDEX. */
  private static final String INDEX = "lead";

  /** The Constant TYPE. */
  private static final String TYPE = "lead";

  /** The client. */
  @Autowired
  private RestHighLevelClient client;

  /** The object mapper. */
  @Autowired
  private ObjectMapper objectMapper;


  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.CONNECTION_CREATED_EVENT;
  }

  /**
   * Perform action.
   *
   * @param eeaEventVO the eea event VO
   */
  @Override
  public void execute(EEAEventVO eeaEventVO) {

    ElasticSearchData data = new ElasticSearchData();
    EntityEvent entityEvent = new EntityEvent();

    ConnectionDataVO conection = (ConnectionDataVO) eeaEventVO.getData().get("connectionDataVO");

    UUID uuid = UUID.randomUUID();

    String datsetname = (String) eeaEventVO.getData().get("dataset_id");

    data.setId(uuid.toString());
    data.setRegisterUserName(conection.getUser());
    entityEvent.setEntityName(datsetname);
    entityEvent.setEventType(eeaEventVO.getEventType().name());
    entityEvent.setEntityURL(conection.getConnectionString() + "/" + conection.getSchema());

    data.setEntityEvent(entityEvent);


    // Start Save Procces.
    Map<String, Object> ElasticSearchDataMapper = objectMapper.convertValue(data, Map.class);

    IndexRequest indexRequest =
        new IndexRequest(INDEX, TYPE, data.getId()).source(ElasticSearchDataMapper);

    IndexResponse indexResponse;
    try {
      indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
      new ResponseEntity(indexResponse.getResult().name(), HttpStatus.CREATED);
    } catch (IOException e) {
      e.printStackTrace();
    } ;


  }



}
